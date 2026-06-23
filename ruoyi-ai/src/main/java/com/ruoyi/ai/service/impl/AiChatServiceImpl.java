package com.ruoyi.ai.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.ai.config.OllamaProperties;
import com.ruoyi.ai.domain.AiAgentMemory;
import com.ruoyi.ai.domain.AiChatRequest;
import com.ruoyi.ai.domain.AiChatResponse;
import com.ruoyi.ai.domain.AiKnowledgeHit;
import com.ruoyi.ai.domain.AiToolTrace;
import com.ruoyi.ai.memory.InventorySnapshotMemoryHandler;
import com.ruoyi.ai.service.IAiAgentMemoryService;
import com.ruoyi.ai.service.IAiChatService;
import com.ruoyi.ai.service.IAiRagService;
import com.ruoyi.ai.tool.AiTool;
import com.ruoyi.ai.tool.AiToolRegistry;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class AiChatServiceImpl implements IAiChatService
{
    /**
     * 识别 ERP 中常见的业务编码，例如 KDS-FLOW-002 或 KDS-FLOW-002-GRAY-120。
     * 注意：这里只做通用编码抽取，不判断编码属于款式、SKU、物料还是单据。
     */
    private static final Pattern BUSINESS_CODE_PATTERN = Pattern.compile("(?<![A-Za-z0-9])[A-Za-z0-9]+(?:-[A-Za-z0-9]+)+(?![A-Za-z0-9])");

    private final OllamaProperties properties;

    private final HttpClient httpClient;

    @Autowired
    private IAiRagService ragService;

    @Autowired
    private AiToolRegistry toolRegistry;

    @Autowired
    private IAiAgentMemoryService agentMemoryService;

    public AiChatServiceImpl(OllamaProperties properties)
    {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .build();
    }

    @Override
    public AiChatResponse chat(AiChatRequest request)
    {
        if (!properties.isEnabled())
        {
            throw new ServiceException("AI 服务未启用");
        }

        List<AiKnowledgeHit> references = shouldUseRag(request) ? ragService.retrieve(request.getPrompt()) : new ArrayList<>();
        if (shouldUseAgent(request) && toolRegistry.hasTools())
        {
            return chatWithAgent(request, references);
        }

        JSONObject body = new JSONObject();
        body.put("model", getRequestModel(request));
        body.put("stream", false);
        body.put("messages", buildMessages(request, references));

        if (properties.getTemperature() != null)
        {
            JSONObject options = new JSONObject();
            options.put("temperature", properties.getTemperature());
            body.put("options", options);
        }

        try
        {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(normalizeBaseUrl(properties.getBaseUrl()) + "/api/chat"))
                    .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300)
            {
                throw new ServiceException("Ollama 调用失败: HTTP " + response.statusCode() + " " + abbreviate(response.body()));
            }
            AiChatResponse result = parseResponse(response.body());
            result.setReferences(references);
            return result;
        }
        catch (IOException e)
        {
            throw new ServiceException("无法连接 Ollama 服务: " + e.getMessage());
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new ServiceException("Ollama 调用被中断");
        }
    }

    private AiChatResponse chatWithAgent(AiChatRequest request, List<AiKnowledgeHit> references)
    {
        String model = getRequestModel(request);

        // 库存变化类问题依赖历史快照，应优先处理，避免被普通库存查询规则抢先执行。
        AiChatResponse memoryCompareResponse = buildInventoryMemoryCompareResponse(request, references, model);
        if (memoryCompareResponse != null)
        {
            return memoryCompareResponse;
        }

        // 款式物料齐套分析是确定性业务计算，命中后直接由后端工具给出结构化结果。
        AiToolTrace materialSufficiencyTrace = buildStyleMaterialSufficiencyTrace(request.getPrompt());
        if (materialSufficiencyTrace != null)
        {
            AiChatResponse fixedResponse = buildFixedStyleMaterialSufficiencyResponse(model, references, materialSufficiencyTrace);
            if (fixedResponse != null)
            {
                return fixedResponse;
            }
        }

        // 常见库存查询使用规则兜底，减少本地模型未按 tool_call JSON 输出导致的漏调用。
        AiToolTrace directTrace = buildDirectToolTrace(request.getPrompt());
        if (directTrace != null)
        {
            rememberToolTrace(request, directTrace);
            AiChatResponse fixedResponse = buildFixedToolResponse(model, references, directTrace);
            if (fixedResponse != null)
            {
                return fixedResponse;
            }
            AiChatResponse directResponse = callOllama(model, buildAgentFinalMessages(request, references, directTrace));
            directResponse.setReferences(references);
            List<AiToolTrace> traces = new ArrayList<>();
            traces.add(directTrace);
            directResponse.setToolCalls(traces);
            return directResponse;
        }

        // 有库存查询意图但缺少可定位对象时直接追问，不让模型编造查询结果。
        if (shouldAskInventoryClarification(request.getPrompt()))
        {
            AiChatResponse clarification = new AiChatResponse();
            clarification.setModel(model);
            clarification.setReferences(references);
            clarification.setContent("你想查库存，我需要再确认一下具体对象。请提供物料或成衣的编码/名称，例如：KDS-FLOW-002、FAB-COT-0001，或者说明要查全部物料库存。");
            return clarification;
        }

        /*
         * 第一步：让模型做“决策”。
         *
         * 这一步不直接回答用户，而是要求模型输出 JSON：
         * - final_answer：不需要工具，直接回答；
         * - tool_call：需要调用 ERP 工具，给出工具名和参数。
         */
        AiChatResponse decisionResponse = callOllama(model, buildAgentDecisionMessages(request, references));
        JSONObject decision = parseAgentDecision(decisionResponse.getContent());
        if (!StringUtils.equals(decision.getString("type"), "tool_call"))
        {
            if (StringUtils.isNotBlank(decision.getString("content")))
            {
                decisionResponse.setContent(decision.getString("content"));
            }
            decisionResponse.setReferences(references);
            return decisionResponse;
        }

        String toolName = decision.getString("tool");
        JSONObject arguments = decision.getJSONObject("arguments");
        if (arguments == null)
        {
            arguments = new JSONObject();
        }

        /*
         * 第二步：后端执行工具。
         *
         * 注意：工具执行发生在后端，模型只负责“选择工具和参数”。这样可以复用 ERP
         * 现有 Service、权限、校验和事务能力，避免让模型直接访问数据库。
         */
        AiToolTrace trace = executeTool(toolName, arguments);
        List<AiToolTrace> traces = new ArrayList<>();
        traces.add(trace);
        rememberToolTrace(request, trace);
        AiChatResponse fixedResponse = buildFixedToolResponse(model, references, trace);
        if (fixedResponse != null)
        {
            return fixedResponse;
        }

        /*
         * 第三步：把工具结果回填给模型。
         *
         * 模型根据真实 ERP 数据生成面向用户的自然语言回答。这里要求模型不要暴露内部
         * JSON 和工具名，避免用户看到调试格式。
         */
        AiChatResponse finalResponse = callOllama(model, buildAgentFinalMessages(request, references, trace));
        finalResponse.setReferences(references);
        finalResponse.setToolCalls(traces);
        return finalResponse;
    }

    private AiToolTrace buildStyleMaterialSufficiencyTrace(String prompt)
    {
        String text = StringUtils.defaultString(prompt);
        if (!isStyleMaterialSufficiencyIntent(text))
        {
            return null;
        }
        // 参数抽取只负责把自然语言归一到工具参数；是否能定位唯一 SKU 由工具内部判定。
        JSONObject arguments = buildStyleMaterialSufficiencyArguments(text);
        return executeTool("query_style_material_sufficiency", arguments);
    }

    private AiChatResponse buildInventoryMemoryCompareResponse(AiChatRequest request, List<AiKnowledgeHit> references, String model)
    {
        String text = StringUtils.defaultString(request.getPrompt());
        if (!isInventoryChangeIntent(text))
        {
            return null;
        }

        JSONObject arguments = buildInventoryCompareArguments(text);
        String memoryKey = null;
        if (arguments != null)
        {
            memoryKey = StringUtils.defaultIfBlank(arguments.getString("itemCode"), arguments.getString("itemName"));
        }

        /*
         * memoryKey 为空时表示“刚才/上次查询的库存”等引用式问题，
         * 此时查询当前用户最近一条库存快照；不为空则查指定业务对象的最近快照。
         */
        AiAgentMemory previous = agentMemoryService.selectLatestValidMemory(
                request.getOperName(), InventorySnapshotMemoryHandler.MEMORY_TYPE, memoryKey, request.getSessionId());
        if (previous == null)
        {
            AiChatResponse response = new AiChatResponse();
            response.setModel(model);
            response.setReferences(references);
            response.setContent(StringUtils.isBlank(memoryKey) ? "未找到你之前的库存查询记录，无法对比变化。" : "未找到“" + memoryKey + "”之前的库存查询记录，无法对比变化。");
            return response;
        }
        if (arguments == null)
        {
            // 用户未提供本次查询条件时，复用上次工具参数重新查询当前库存，保证对比口径一致。
            arguments = JSON.parseObject(previous.getArgumentsJson());
        }
        memoryKey = StringUtils.defaultIfBlank(previous.getMemoryKey(), StringUtils.defaultIfBlank(arguments.getString("itemCode"), arguments.getString("itemName")));

        AiToolTrace currentTrace = executeTool("query_inventory", arguments);
        rememberToolTrace(request, currentTrace);

        AiChatResponse response = new AiChatResponse();
        response.setModel(model);
        response.setReferences(references);
        List<AiToolTrace> traces = new ArrayList<>();
        traces.add(currentTrace);
        response.setToolCalls(traces);
        response.setContent(compareInventorySnapshot(memoryKey, previous.getResultJson(), currentTrace.getResult(), arguments));
        return response;
    }

    private AiToolTrace buildDirectToolTrace(String prompt)
    {
        JSONObject inventoryArguments = buildDirectInventoryArguments(prompt);
        if (inventoryArguments == null)
        {
            return null;
        }
        return executeTool("query_inventory", inventoryArguments);
    }

    private JSONObject buildDirectInventoryArguments(String prompt)
    {
        String text = StringUtils.defaultString(prompt);
        if (!isInventoryLookupIntent(text))
        {
            return null;
        }

        JSONObject arguments = new JSONObject();
        String code = extractBusinessCode(text);
        if (StringUtils.isNotBlank(code))
        {
            arguments.put("itemCode", code);
        }
        String itemName = extractInventoryItemName(text);
        if (StringUtils.isBlank(code) && StringUtils.isNotBlank(itemName) && !isMemoryReferenceOnly(itemName))
        {
            arguments.put("itemName", itemName);
        }
        if (containsAny(text, "成衣", "款式", "衣服", "服装", "SKU", "只需要成衣", "只看成衣", "只要成衣"))
        {
            arguments.put("itemType", "成衣");
        }
        else if (containsAny(text, "物料", "面料", "辅料", "包装"))
        {
            arguments.put("itemType", "物料");
        }
        if (containsAny(text, "成衣仓", "成品仓"))
        {
            arguments.put("warehouseName", "成衣");
        }
        else if (containsAny(text, "物料仓"))
        {
            arguments.put("warehouseName", "物料");
        }
        arguments.put("limit", 20);

        /*
         * 有明确编码、仓库筛选，或者用户明确要“全部/汇总/列表”时才直接查 ERP。
         * 只有“这个物料还剩多少”这类缺少对象的问题，会先追问，避免盲查一大批数据。
         */
        if (StringUtils.isNotBlank(arguments.getString("itemCode"))
                || StringUtils.isNotBlank(arguments.getString("itemName"))
                || StringUtils.isNotBlank(arguments.getString("warehouseName"))
                || containsAny(text, "全部", "所有", "汇总", "列表", "有哪些"))
        {
            return arguments;
        }
        return null;
    }

    private JSONObject buildStyleMaterialSufficiencyArguments(String text)
    {
        JSONObject arguments = new JSONObject();
        String code = extractBusinessCode(text);
        if (StringUtils.isNotBlank(code))
        {
            if (code.split("-").length >= 4)
            {
                arguments.put("skuCode", code);
            }
            else
            {
                arguments.put("styleNo", code);
            }
        }

        String styleName = extractStyleNameForMaterialSufficiency(text);
        if (StringUtils.isNotBlank(styleName))
        {
            arguments.put("styleName", styleName);
        }
        String colorName = extractAfterKeyword(text, "颜色");
        if (StringUtils.isNotBlank(colorName))
        {
            arguments.put("colorName", colorName);
        }
        String sizeName = extractSizeName(text);
        if (StringUtils.isNotBlank(sizeName))
        {
            arguments.put("sizeName", sizeName);
        }
        return arguments;
    }

    private JSONObject buildInventoryCompareArguments(String text)
    {
        JSONObject arguments = new JSONObject();
        String code = extractBusinessCode(text);
        if (StringUtils.isNotBlank(code))
        {
            arguments.put("itemCode", code);
        }
        String itemName = extractInventoryItemName(text);
        if (StringUtils.isBlank(code) && StringUtils.isNotBlank(itemName))
        {
            arguments.put("itemName", itemName);
        }
        if (containsAny(text, "成衣", "款式", "衣服", "服装", "SKU", "只需要成衣", "只看成衣", "只要成衣"))
        {
            arguments.put("itemType", "成衣");
        }
        else if (containsAny(text, "物料", "面料", "辅料", "包装"))
        {
            arguments.put("itemType", "物料");
        }
        arguments.put("limit", 20);
        if (StringUtils.isBlank(arguments.getString("itemCode")) && StringUtils.isBlank(arguments.getString("itemName")))
        {
            return null;
        }
        return arguments;
    }

    private boolean shouldAskInventoryClarification(String prompt)
    {
        String text = StringUtils.defaultString(prompt);
        if (!isInventoryLookupIntent(text))
        {
            return false;
        }
        if (StringUtils.isNotBlank(extractBusinessCode(text)))
        {
            return false;
        }
        if (StringUtils.isNotBlank(extractInventoryItemName(text)))
        {
            return false;
        }
        if (containsAny(text, "全部", "所有", "汇总", "列表", "有哪些", "怎么", "如何", "操作", "功能"))
        {
            return false;
        }
        return containsAny(text, "这个", "该", "某个", "物料", "成衣", "款式", "衣服", "面料", "辅料", "包装");
    }

    private boolean isInventoryLookupIntent(String text)
    {
        boolean hasInventoryWord = containsAny(text, "库存", "可用", "锁定", "仓库", "余量", "存量");
        boolean hasRemainingWord = containsAny(text, "还剩", "剩余", "剩下", "剩多少", "还有多少", "够不够", "可发", "可出");
        boolean hasItemWord = containsAny(text, "成衣", "物料", "款式", "衣服", "服装", "面料", "辅料", "包装", "SKU");
        return hasInventoryWord || (hasRemainingWord && hasItemWord);
    }

    private boolean isStyleMaterialSufficiencyIntent(String text)
    {
        /*
         * “能、可以、多少”本身过于宽泛，必须同时出现物料/BOM上下文和款式/SKU/生产上下文，
         * 才视为“查询物料是否足够生产”的业务意图。
         * 对“拼色连帽卫衣的物料是否充足”这类没有显式“款式/SKU”的问法，
         * 只要能抽取到物料语义前面的款式名称，也认为具备款式上下文。
         */
        boolean hasStyleWord = containsAny(text, "款式", "款号", "SKU", "当前sku", "当前SKU", "成衣", "衣服", "服装", "生产");
        boolean hasMaterialWord = containsAny(text, "物料", "面料", "辅料", "BOM", "材料");
        boolean hasSufficiencyWord = containsAny(text, "充足", "够不够", "够不", "是否够", "能", "可以", "生产", "多少", "能生产", "可生产", "可以生产", "生产多少", "能做多少", "齐套");
        boolean hasStyleName = StringUtils.isNotBlank(extractStyleNameForMaterialSufficiency(text));
        return hasMaterialWord && hasSufficiencyWord && (hasStyleWord || hasStyleName || StringUtils.isNotBlank(extractBusinessCode(text)));
    }

    private boolean isInventoryChangeIntent(String text)
    {
        return isInventoryLookupIntent(text) && containsAny(text, "变化", "变了", "有没有变", "是否有变", "比上次", "和上次", "刚才", "之前", "分钟前", "十分钟前");
    }

    private void rememberToolTrace(AiChatRequest request, AiToolTrace trace)
    {
        try
        {
            agentMemoryService.rememberToolTrace(request, trace);
        }
        catch (RuntimeException ignored)
        {
            // 记忆失败不应影响主问答流程；审计日志仍会记录本次对话结果。
        }
    }

    private String compareInventorySnapshot(String memoryKey, String previousResultJson, JSONObject currentResult, JSONObject filter)
    {
        JSONObject previousResult = JSON.parseObject(previousResultJson);
        /*
         * 历史快照可能包含更宽的范围，例如“卫衣”同时命中成衣和物料。
         * 对比时必须用本次条件同时过滤历史和当前结果，否则会把不同口径误判为库存变化。
         */
        InventoryTotals previous = sumInventory(previousResult, filter);
        InventoryTotals current = sumInventory(currentResult, filter);

        if (previous.count == 0 && current.count == 0)
        {
            return "没有变化，前后都未查询到“" + memoryKey + "”的库存记录。";
        }
        if (previous.count > 0 && current.count == 0)
        {
            return "有变化。“" + memoryKey + "”之前有库存记录，现在未查询到库存记录。";
        }
        if (previous.count == 0)
        {
            return "有变化。“" + memoryKey + "”之前未查询到库存记录，现在可用库存为 " + current.availableQty + "，锁定库存为 " + current.lockedQty + "。";
        }

        BigDecimal availableDiff = current.availableQty.subtract(previous.availableQty);
        BigDecimal lockedDiff = current.lockedQty.subtract(previous.lockedQty);
        if (availableDiff.compareTo(BigDecimal.ZERO) == 0 && lockedDiff.compareTo(BigDecimal.ZERO) == 0)
        {
            return "没有变化。“" + memoryKey + "”当前可用库存仍为 " + current.availableQty + "，锁定库存仍为 " + current.lockedQty + "。";
        }
        return "有变化。“" + memoryKey + "”可用库存从 " + previous.availableQty + " 变为 " + current.availableQty
                + "（变化 " + formatSigned(availableDiff) + "），锁定库存从 " + previous.lockedQty + " 变为 " + current.lockedQty
                + "（变化 " + formatSigned(lockedDiff) + "）。";
    }

    private InventoryTotals sumInventory(JSONObject result, JSONObject filter)
    {
        InventoryTotals totals = new InventoryTotals();
        if (result == null)
        {
            return totals;
        }
        JSONArray items = result.getJSONArray("items");
        if (items == null)
        {
            return totals;
        }
        Map<String, JSONObject> uniqueItems = new LinkedHashMap<>();
        for (int i = 0; i < items.size(); i++)
        {
            JSONObject item = items.getJSONObject(i);
            if (item == null)
            {
                continue;
            }
            if (!matchesInventoryFilter(item, filter))
            {
                continue;
            }
            // 同一库存对象可能因查询范围重叠被重复带出，用业务维度去重后再汇总数量。
            String key = StringUtils.defaultString(item.getString("warehouseName")) + "|"
                    + StringUtils.defaultString(item.getString("itemCode")) + "|"
                    + StringUtils.defaultString(item.getString("colorName")) + "|"
                    + StringUtils.defaultString(item.getString("sizeName")) + "|"
                    + StringUtils.defaultString(item.getString("batchNo"));
            uniqueItems.put(key, item);
        }
        totals.count = uniqueItems.size();
        for (JSONObject item : uniqueItems.values())
        {
            totals.availableQty = totals.availableQty.add(item.getBigDecimal("availableQty") == null ? BigDecimal.ZERO : item.getBigDecimal("availableQty"));
            totals.lockedQty = totals.lockedQty.add(item.getBigDecimal("lockedQty") == null ? BigDecimal.ZERO : item.getBigDecimal("lockedQty"));
        }
        return totals;
    }

    private boolean matchesInventoryFilter(JSONObject item, JSONObject filter)
    {
        if (filter == null)
        {
            return true;
        }
        if (StringUtils.isNotBlank(filter.getString("itemType"))
                && !StringUtils.equals(filter.getString("itemType"), item.getString("itemType")))
        {
            return false;
        }
        if (StringUtils.isNotBlank(filter.getString("itemCode"))
                && !StringUtils.defaultString(item.getString("itemCode")).contains(filter.getString("itemCode")))
        {
            return false;
        }
        if (StringUtils.isNotBlank(filter.getString("itemName"))
                && !StringUtils.defaultString(item.getString("itemName")).contains(filter.getString("itemName")))
        {
            return false;
        }
        if (StringUtils.isNotBlank(filter.getString("warehouseName"))
                && !StringUtils.defaultString(item.getString("warehouseName")).contains(filter.getString("warehouseName")))
        {
            return false;
        }
        return true;
    }

    private String formatSigned(BigDecimal value)
    {
        if (value.compareTo(BigDecimal.ZERO) > 0)
        {
            return "+" + value;
        }
        return value.toString();
    }

    private AiChatResponse buildFixedToolResponse(String model, List<AiKnowledgeHit> references, AiToolTrace trace)
    {
        if (!StringUtils.equals("query_inventory", trace.getToolName()))
        {
            return null;
        }
        JSONObject result = trace.getResult();
        if (result == null || result.getIntValue("total") != 0)
        {
            return null;
        }

        // 查无库存记录时由后端固定短答，避免模型扩写操作建议或生成无关说明。
        JSONObject arguments = trace.getArguments();
        String item = StringUtils.defaultIfBlank(arguments.getString("itemCode"), arguments.getString("itemName"));
        if (StringUtils.isBlank(item))
        {
            item = StringUtils.defaultIfBlank(arguments.getString("itemType"), "该条件");
        }

        AiChatResponse response = new AiChatResponse();
        response.setModel(model);
        response.setReferences(references);
        List<AiToolTrace> traces = new ArrayList<>();
        traces.add(trace);
        response.setToolCalls(traces);
        JSONObject matchedStyleSku = result.getJSONObject("matchedStyleSku");
        if (matchedStyleSku != null)
        {
            response.setContent("SKU“" + matchedStyleSku.getString("skuCode") + "”存在（"
                    + StringUtils.defaultString(matchedStyleSku.getString("styleName")) + " / "
                    + StringUtils.defaultString(matchedStyleSku.getString("colorName")) + " / "
                    + StringUtils.defaultString(matchedStyleSku.getString("sizeName"))
                    + "），但当前未查询到库存记录。");
            return response;
        }
        response.setContent("未查询到“" + item + "”的库存记录。");
        return response;
    }

    private AiChatResponse buildFixedStyleMaterialSufficiencyResponse(String model, List<AiKnowledgeHit> references, AiToolTrace trace)
    {
        if (!StringUtils.equals("query_style_material_sufficiency", trace.getToolName()))
        {
            return null;
        }

        // 物料齐套结果属于确定性业务计算，统一在后端格式化，避免模型改写数量口径。
        JSONObject result = trace.getResult();
        String status = result.getString("status");
        AiChatResponse response = new AiChatResponse();
        response.setModel(model);
        response.setReferences(references);
        List<AiToolTrace> traces = new ArrayList<>();
        traces.add(trace);
        response.setToolCalls(traces);

        if (StringUtils.equals("need_sku", status))
        {
            response.setContent(buildNeedSkuContent(result));
            return response;
        }
        if (StringUtils.equals("not_found", status))
        {
            response.setContent("未找到匹配的款式 SKU。请提供更具体的款号、SKU编码、颜色或尺码。");
            return response;
        }
        if (StringUtils.equals("no_bom", status) || StringUtils.equals("no_bom_detail", status))
        {
            response.setContent(result.getString("message"));
            return response;
        }
        if (StringUtils.equals("ok", status))
        {
            response.setContent(buildStyleMaterialSufficiencyContent(result));
            return response;
        }
        return null;
    }

    private String buildNeedSkuContent(JSONObject result)
    {
        JSONArray candidates = result.getJSONArray("candidates");
        StringBuilder content = new StringBuilder("请先确认具体 SKU，我再计算物料是否充足。");
        if (candidates != null && !candidates.isEmpty())
        {
            content.append("\n可选 SKU：");
            for (int i = 0; i < candidates.size(); i++)
            {
                JSONObject item = candidates.getJSONObject(i);
                content.append("\n").append(i + 1).append(". ")
                        .append(item.getString("skuCode")).append("（")
                        .append(StringUtils.defaultIfBlank(item.getString("styleName"), item.getString("styleNo")));
                if (StringUtils.isNotBlank(item.getString("colorName")) || StringUtils.isNotBlank(item.getString("sizeName")))
                {
                    content.append("，").append(StringUtils.defaultString(item.getString("colorName")))
                            .append(" / ").append(StringUtils.defaultString(item.getString("sizeName")));
                }
                content.append("）");
            }
        }
        return content.toString();
    }

    private String buildStyleMaterialSufficiencyContent(JSONObject result)
    {
        JSONObject bottleneck = result.getJSONObject("bottleneck");
        StringBuilder content = new StringBuilder();
        if (StringUtils.equals("style", result.getString("targetType")))
        {
            content.append(result.getString("styleName")).append("（款号：")
                    .append(result.getString("styleNo")).append("）按当前物料库存最多可生产 ")
                    .append(result.getBigDecimal("maxProduceQty")).append(" 件。");
            if (bottleneck != null)
            {
                content.append("\n瓶颈物料：").append(bottleneck.getString("materialName"))
                        .append("，当前可用 ").append(bottleneck.getBigDecimal("availableQty"))
                        .append(StringUtils.defaultString(bottleneck.getString("unitName")))
                        .append("，单件含损耗用量 ").append(bottleneck.getBigDecimal("requiredPerPiece"))
                        .append(StringUtils.defaultString(bottleneck.getString("unitName"))).append("。");
            }
            content.append("\n以上为当前系统数据测算，后续生产前仍需结合实际仓库、批次质量和最新出入库情况确认。");
            return content.toString();
        }
        content.append(result.getString("styleName")).append(" ")
                .append(result.getString("colorName")).append(" / ")
                .append(result.getString("sizeName")).append("（")
                .append(result.getString("skuCode")).append("）按当前物料库存最多可生产 ")
                .append(result.getBigDecimal("maxProduceQty")).append(" 件。");
        if (bottleneck != null)
        {
            content.append("\n瓶颈物料：").append(bottleneck.getString("materialName"))
                    .append("，当前可用 ").append(bottleneck.getBigDecimal("availableQty"))
                    .append(StringUtils.defaultString(bottleneck.getString("unitName")))
                    .append("，单件含损耗用量 ").append(bottleneck.getBigDecimal("requiredPerPiece"))
                    .append(StringUtils.defaultString(bottleneck.getString("unitName"))).append("。");
        }
        content.append("\n以上为当前系统数据测算，后续生产前仍需结合实际仓库、批次质量和最新出入库情况确认。");
        return content.toString();
    }

    private AiToolTrace executeTool(String toolName, JSONObject arguments)
    {
        // 所有工具都必须先经过注册表白名单解析，模型或规则不能直接调用任意类/SQL。
        AiTool tool = toolRegistry.getRequiredTool(toolName);
        JSONObject toolResult = tool.execute(arguments);

        AiToolTrace trace = new AiToolTrace();
        trace.setToolName(toolName);
        trace.setArguments(arguments);
        trace.setResult(toolResult);
        return trace;
    }

    private String extractBusinessCode(String text)
    {
        Matcher matcher = BUSINESS_CODE_PATTERN.matcher(StringUtils.defaultString(text));
        return matcher.find() ? matcher.group() : null;
    }

    private String extractInventoryItemName(String text)
    {
        String value = StringUtils.defaultString(text).trim();
        // 只截取库存语义前面的对象短语；后续 clean 方法会去掉“查询/成衣/库存”等非名称词。
        String[] suffixes = {"的可用库存", "可用库存", "的库存", "库存", "还剩多少", "还有多少", "剩余多少", "剩多少"};
        for (String suffix : suffixes)
        {
            int index = value.indexOf(suffix);
            if (index > 0)
            {
                return cleanInventoryItemName(value.substring(0, index));
            }
        }
        return null;
    }

    private String extractStyleNameForMaterialSufficiency(String text)
    {
        String value = StringUtils.defaultString(text).trim();
        // 针对“拼色连帽卫衣的物料够吗/能生产多少”这类问法抽取款式名称。
        String[] suffixes = {"的物料", "物料是否", "物料够", "物料充足", "材料是否", "材料够", "能生产", "可生产", "可以生产", "能做", "可以做", "生产多少", "能生产多少", "可以生产多少"};
        for (String suffix : suffixes)
        {
            int index = value.indexOf(suffix);
            if (index > 0)
            {
                return cleanStyleName(value.substring(0, index));
            }
        }
        return null;
    }

    private String cleanStyleName(String text)
    {
        String value = StringUtils.defaultString(text).trim();
        value = value.replaceFirst("^(帮我|帮忙|请|查询|查一下|查下|看一下|看看|我想查|我要查)", "");
        value = value.replace("款式", "")
                .replace("款号", "")
                .replace("成衣", "")
                .replace("SKU", "")
                .replace("是否", "")
                .replace("的", "");
        value = value.replaceAll("\\s+", " ").trim();
        if (StringUtils.isBlank(value) || value.length() > 50 || StringUtils.isNotBlank(extractBusinessCode(value)))
        {
            return null;
        }
        return value;
    }

    private String extractAfterKeyword(String text, String keyword)
    {
        String value = StringUtils.defaultString(text);
        int index = value.indexOf(keyword);
        if (index < 0)
        {
            return null;
        }
        String tail = value.substring(index + keyword.length()).trim();
        tail = tail.replaceFirst("^(是|为|:|：)", "").trim();
        int end = tail.indexOf(" ");
        return cleanShortSlot(end > 0 ? tail.substring(0, end) : tail);
    }

    private String extractSizeName(String text)
    {
        /*
         * 尺码不能从业务编码内部截取，例如 KDS-FLOW-002 中的 002 不是尺码。
         * 因此要求尺码前后不能紧贴字母、数字或连字符；“120码”“ 120 ”仍可识别。
         */
        Matcher matcher = Pattern.compile("(?<![A-Za-z0-9-])(\\d{2,3}|XS|S|M|L|XL|XXL|XXXL)(码|#)?(?![A-Za-z0-9-])", Pattern.CASE_INSENSITIVE).matcher(StringUtils.defaultString(text));
        return matcher.find() ? matcher.group(1).toUpperCase() : null;
    }

    private String cleanShortSlot(String text)
    {
        String value = StringUtils.defaultString(text).trim();
        if (StringUtils.isBlank(value) || value.length() > 20)
        {
            return null;
        }
        return value;
    }

    private String cleanInventoryItemName(String text)
    {
        String value = StringUtils.defaultString(text).trim();
        value = value.replaceFirst("^(帮我|帮忙|请|查询|查一下|查下|看一下|看看|我想查|我要查|我刚查询的|刚查询的|刚才查询的|刚查的|上次查询的|之前查询的)", "");
        value = value.replaceFirst("^(这个|该|某个)", "");
        value = value.replace("查询", "")
                .replace("查一下", "")
                .replace("查下", "")
                .replace("看一下", "")
                .replace("看看", "")
                .replace("成衣", "")
                .replace("物料", "")
                .replace("库存", "")
                .replace("数据", "")
                .replace("信息", "")
                .replace("刚才", "")
                .replace("刚刚", "")
                .replace("上次", "")
                .replace("之前", "")
                .replace("有变化", "")
                .replace("变化", "")
                .replace("有", "")
                .replace("的", "");
        value = value.replaceAll("\\s+", " ");
        value = value.trim();
        if (StringUtils.isBlank(value) || value.length() > 50 || containsAny(value, "怎么", "如何", "操作", "功能"))
        {
            return null;
        }
        return value;
    }

    private boolean isMemoryReferenceOnly(String text)
    {
        String value = StringUtils.defaultString(text).trim();
        return StringUtils.isBlank(value) || containsAny(value, "刚才", "刚刚", "上次", "之前", "变化", "查询");
    }

    private boolean containsAny(String text, String... keywords)
    {
        String value = StringUtils.defaultString(text);
        for (String keyword : keywords)
        {
            if (value.contains(keyword))
            {
                return true;
            }
        }
        return false;
    }

    private static class InventoryTotals
    {
        private int count;

        private BigDecimal availableQty = BigDecimal.ZERO;

        private BigDecimal lockedQty = BigDecimal.ZERO;
    }

    @Override
    public List<String> listModels()
    {
        if (!properties.isEnabled())
        {
            throw new ServiceException("AI 服务未启用");
        }

        try
        {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(normalizeBaseUrl(properties.getBaseUrl()) + "/api/tags"))
                    .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300)
            {
                throw new ServiceException("Ollama 模型列表获取失败: HTTP " + response.statusCode() + " " + abbreviate(response.body()));
            }

            JSONObject json = JSON.parseObject(response.body());
            JSONArray models = json.getJSONArray("models");
            List<String> names = new ArrayList<>();
            addChatModel(names, properties.getModel());
            if (models != null)
            {
                for (int i = 0; i < models.size(); i++)
                {
                    JSONObject model = models.getJSONObject(i);
                    if (model != null && StringUtils.isNotBlank(model.getString("name")))
                    {
                        addChatModel(names, model.getString("name"));
                    }
                }
            }
            return names;
        }
        catch (IOException e)
        {
            throw new ServiceException("无法连接 Ollama 服务: " + e.getMessage());
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new ServiceException("Ollama 调用被中断");
        }
    }

    private JSONArray buildMessages(AiChatRequest request, List<AiKnowledgeHit> references)
    {
        JSONArray messages = new JSONArray();
        String systemPrompt = StringUtils.isNotBlank(request.getSystemPrompt()) ? request.getSystemPrompt() : properties.getSystemPrompt();
        if (StringUtils.isNotBlank(systemPrompt))
        {
            JSONObject system = new JSONObject();
            system.put("role", "system");
            system.put("content", systemPrompt);
            messages.add(system);
        }

        JSONObject user = new JSONObject();
        user.put("role", "user");
        user.put("content", buildUserPrompt(request.getPrompt(), references));
        messages.add(user);
        return messages;
    }

    private JSONArray buildAgentDecisionMessages(AiChatRequest request, List<AiKnowledgeHit> references)
    {
        JSONArray messages = new JSONArray();

        JSONObject system = new JSONObject();
        system.put("role", "system");
        system.put("content", buildAgentDecisionSystemPrompt(request));
        messages.add(system);

        JSONObject user = new JSONObject();
        user.put("role", "user");
        user.put("content", buildUserPrompt(request.getPrompt(), references));
        messages.add(user);
        return messages;
    }

    private JSONArray buildAgentFinalMessages(AiChatRequest request, List<AiKnowledgeHit> references, AiToolTrace trace)
    {
        JSONArray messages = new JSONArray();

        JSONObject system = new JSONObject();
        system.put("role", "system");
        system.put("content", "你是一丫一 ERP 系统中的智能业务助手。请只根据 ERP 工具返回的数据回答，要求简洁、准确。库存查询最多三句话；不要输出操作步骤、温馨提示、泛泛建议；不要暴露工具名、JSON、内部字段名或调用过程。");
        messages.add(system);

        JSONObject user = new JSONObject();
        StringBuilder content = new StringBuilder();
        content.append(buildUserPrompt(request.getPrompt(), references));
        content.append("\n\n【ERP工具查询结果】\n");
        content.append(JSON.toJSONString(trace.getResult()));
        content.append("\n\n请基于以上真实 ERP 数据回答用户。若有库存数据，直接列出关键库存结果；若没有数据，只说未查询到对应库存记录。");
        user.put("role", "user");
        user.put("content", content.toString());
        messages.add(user);
        return messages;
    }

    private String buildAgentDecisionSystemPrompt(AiChatRequest request)
    {
        StringBuilder prompt = new StringBuilder();
        String baseSystemPrompt = StringUtils.isNotBlank(request.getSystemPrompt()) ? request.getSystemPrompt() : properties.getSystemPrompt();
        if (StringUtils.isNotBlank(baseSystemPrompt))
        {
            prompt.append(baseSystemPrompt).append("\n\n");
        }

        prompt.append("你现在具备 ERP 工具调用能力。请先判断用户问题是否需要查询 ERP 实时数据。\n");
        prompt.append("可用工具如下：\n");
        prompt.append(JSON.toJSONString(toolRegistry.describeTools())).append("\n\n");
        prompt.append("如果需要工具，请只输出 JSON，不要输出 Markdown，不要解释：\n");
        prompt.append("{\"type\":\"tool_call\",\"tool\":\"query_inventory\",\"arguments\":{\"itemCode\":\"示例编码\",\"limit\":10}}\n");
        prompt.append("如果不需要工具，请只输出 JSON：\n");
        prompt.append("{\"type\":\"final_answer\",\"content\":\"你的回答\"}\n");
        prompt.append("只能调用上面列出的工具；参数不确定时可以少填，不要编造编码、仓库或批次。");
        return prompt.toString();
    }

    private String buildUserPrompt(String prompt, List<AiKnowledgeHit> references)
    {
        if (references == null || references.isEmpty())
        {
            return prompt;
        }

        StringBuilder content = new StringBuilder();
        content.append("请优先依据下面的一丫一 ERP 知识库内容回答。");
        content.append("如果知识库没有足够依据，请明确说明，并给出需要进一步确认的信息。\n\n");
        content.append("回答时不要暴露、列出或提及“参考知识”“知识库片段”“来源标题”等内部检索信息，只输出面向用户的业务答案。\n\n");
        content.append("【ERP知识库】\n");
        for (int i = 0; i < references.size(); i++)
        {
            AiKnowledgeHit hit = references.get(i);
            content.append(i + 1).append(". ");
            if (StringUtils.isNotBlank(hit.getTitle()))
            {
                content.append(hit.getTitle());
            }
            if (StringUtils.isNotBlank(hit.getModuleName()))
            {
                content.append("（").append(hit.getModuleName()).append("）");
            }
            content.append("\n");
            content.append(hit.getContent()).append("\n\n");
        }
        content.append("【用户问题】\n").append(prompt);
        return content.toString();
    }

    private AiChatResponse parseResponse(String body)
    {
        JSONObject json = JSON.parseObject(body);
        JSONObject message = json.getJSONObject("message");
        if (message == null || StringUtils.isBlank(message.getString("content")))
        {
            throw new ServiceException("Ollama 返回内容为空");
        }

        AiChatResponse result = new AiChatResponse();
        result.setModel(json.getString("model"));
        result.setContent(message.getString("content"));
        result.setTotalDuration(json.getLong("total_duration"));
        result.setPromptEvalCount(json.getInteger("prompt_eval_count"));
        result.setEvalCount(json.getInteger("eval_count"));
        return result;
    }

    private AiChatResponse callOllama(String model, JSONArray messages)
    {
        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("stream", false);
        body.put("messages", messages);

        if (properties.getTemperature() != null)
        {
            JSONObject options = new JSONObject();
            options.put("temperature", properties.getTemperature());
            body.put("options", options);
        }

        try
        {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(normalizeBaseUrl(properties.getBaseUrl()) + "/api/chat"))
                    .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300)
            {
                throw new ServiceException("Ollama 调用失败: HTTP " + response.statusCode() + " " + abbreviate(response.body()));
            }
            return parseResponse(response.body());
        }
        catch (IOException e)
        {
            throw new ServiceException("无法连接 Ollama 服务: " + e.getMessage());
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new ServiceException("Ollama 调用被中断");
        }
    }

    private JSONObject parseAgentDecision(String content)
    {
        try
        {
            JSONObject decision = JSON.parseObject(extractJsonObject(content));
            if (StringUtils.equals(decision.getString("type"), "final_answer"))
            {
                return decision;
            }
            if (StringUtils.equals(decision.getString("type"), "tool_call") && StringUtils.isNotBlank(decision.getString("tool")))
            {
                return decision;
            }
        }
        catch (RuntimeException ignored)
        {
            // 模型偶尔会输出自然语言。解析失败时降级为普通回答，保证对话不中断。
        }

        JSONObject fallback = new JSONObject();
        fallback.put("type", "final_answer");
        fallback.put("content", content);
        return fallback;
    }

    private String extractJsonObject(String content)
    {
        String text = StringUtils.defaultString(content).trim();
        if (text.startsWith("```"))
        {
            text = text.replaceFirst("^```[a-zA-Z]*", "").replaceFirst("```$", "").trim();
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start)
        {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private String getRequestModel(AiChatRequest request)
    {
        String model = StringUtils.isNotBlank(request.getModel()) ? request.getModel() : properties.getModel();
        if (isEmbeddingModel(model))
        {
            throw new ServiceException("当前选择的是向量模型 " + model + "，不能用于对话。请切换为 " + properties.getModel() + " 等聊天模型。");
        }
        return model;
    }

    private void addChatModel(List<String> names, String model)
    {
        if (StringUtils.isBlank(model) || isEmbeddingModel(model) || names.contains(model))
        {
            return;
        }
        names.add(model);
    }

    private boolean isEmbeddingModel(String model)
    {
        return StringUtils.equals(normalizeModelName(model), normalizeModelName(properties.getEmbeddingModel()));
    }

    private String normalizeModelName(String model)
    {
        String normalized = StringUtils.defaultString(model).trim();
        if (normalized.endsWith(":latest"))
        {
            normalized = normalized.substring(0, normalized.length() - ":latest".length());
        }
        return normalized;
    }

    private boolean shouldUseRag(AiChatRequest request)
    {
        return request.getRagEnabled() != null ? request.getRagEnabled() : properties.isRagEnabled();
    }

    private boolean shouldUseAgent(AiChatRequest request)
    {
        return request.getAgentEnabled() == null || request.getAgentEnabled();
    }

    private String normalizeBaseUrl(String baseUrl)
    {
        String normalized = StringUtils.defaultIfBlank(baseUrl, "http://127.0.0.1:11434").trim();
        while (normalized.endsWith("/"))
        {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String abbreviate(String text)
    {
        if (StringUtils.isBlank(text) || text.length() <= 300)
        {
            return text;
        }
        return text.substring(0, 300) + "...";
    }
}
