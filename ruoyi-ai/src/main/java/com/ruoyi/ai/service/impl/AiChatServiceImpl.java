package com.ruoyi.ai.service.impl;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.ai.config.OllamaProperties;
import com.ruoyi.ai.domain.AiChatRequest;
import com.ruoyi.ai.domain.AiChatResponse;
import com.ruoyi.ai.domain.AiKnowledgeHit;
import com.ruoyi.ai.domain.AiToolTrace;
import com.ruoyi.ai.service.IAiChatService;
import com.ruoyi.ai.service.IAiRagService;
import com.ruoyi.ai.tool.AiTool;
import com.ruoyi.ai.tool.AiToolRegistry;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class AiChatServiceImpl implements IAiChatService
{
    private static final Pattern BUSINESS_CODE_PATTERN = Pattern.compile("\\b[A-Za-z0-9]+(?:-[A-Za-z0-9]+)+\\b");

    private final OllamaProperties properties;

    private final HttpClient httpClient;

    @Autowired
    private IAiRagService ragService;

    @Autowired
    private AiToolRegistry toolRegistry;

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

        AiToolTrace directTrace = buildDirectToolTrace(request.getPrompt());
        if (directTrace != null)
        {
            AiChatResponse directResponse = callOllama(model, buildAgentFinalMessages(request, references, directTrace));
            directResponse.setReferences(references);
            List<AiToolTrace> traces = new ArrayList<>();
            traces.add(directTrace);
            directResponse.setToolCalls(traces);
            return directResponse;
        }
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
        if (containsAny(text, "成衣", "款式", "衣服", "服装", "SKU"))
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
                || StringUtils.isNotBlank(arguments.getString("warehouseName"))
                || containsAny(text, "全部", "所有", "汇总", "列表", "有哪些"))
        {
            return arguments;
        }
        return null;
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

    private AiToolTrace executeTool(String toolName, JSONObject arguments)
    {
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
        system.put("content", "你是曹氏 ERP 系统中的智能业务助手。请根据用户问题、知识库内容和 ERP 工具返回的数据，生成简洁、准确的中文回答。不要暴露工具名、JSON、内部字段名或调用过程。");
        messages.add(system);

        JSONObject user = new JSONObject();
        StringBuilder content = new StringBuilder();
        content.append(buildUserPrompt(request.getPrompt(), references));
        content.append("\n\n【ERP工具查询结果】\n");
        content.append(JSON.toJSONString(trace.getResult()));
        content.append("\n\n请基于以上真实 ERP 数据回答用户。");
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
        content.append("请优先依据下面的曹氏 ERP 知识库内容回答。");
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
