package com.ruoyi.ai.memory;

import java.util.Calendar;
import java.util.Date;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.ai.domain.AiAgentMemory;
import com.ruoyi.ai.domain.AiChatRequest;
import com.ruoyi.ai.domain.AiToolTrace;
import com.ruoyi.common.utils.StringUtils;

/**
 * 库存快照记忆。
 *
 * 每次 query_inventory 成功后保存一份快照，后续用户问“和刚才比是否有变化”
 * 时，可以重新查询当前库存并与这份快照做结构化对比。
 */
@Component
public class InventorySnapshotMemoryHandler implements AiMemoryHandler
{
    public static final String MEMORY_TYPE = "inventory_snapshot";

    @Override
    public boolean supports(AiToolTrace trace)
    {
        // 目前只把库存查询结果保存为快照；其他工具要记忆时应新增独立 Handler。
        return trace != null && StringUtils.equals("query_inventory", trace.getToolName()) && trace.getResult() != null;
    }

    @Override
    public AiAgentMemory buildMemory(AiChatRequest request, AiToolTrace trace)
    {
        JSONObject arguments = trace.getArguments();
        String memoryKey = resolveMemoryKey(arguments);
        if (StringUtils.isBlank(memoryKey))
        {
            return null;
        }

        AiAgentMemory memory = new AiAgentMemory();
        memory.setMemoryType(MEMORY_TYPE);
        memory.setMemoryKey(memoryKey);
        memory.setMemoryTitle("库存快照：" + memoryKey);
        memory.setToolName(trace.getToolName());
        memory.setArgumentsJson(JSON.toJSONString(arguments));
        memory.setResultJson(JSON.toJSONString(trace.getResult()));
        memory.setSummary(buildSummary(memoryKey, trace.getResult()));
        memory.setSessionId(request.getSessionId());
        memory.setOperName(request.getOperName());
        // 库存变化快，第一版只保留 1 天快照，避免长期旧数据参与“是否变化”判断。
        memory.setExpireTime(afterDays(1));
        memory.setStatus("0");
        return memory;
    }

    private String resolveMemoryKey(JSONObject arguments)
    {
        if (arguments == null)
        {
            return null;
        }
        String itemCode = arguments.getString("itemCode");
        if (StringUtils.isNotBlank(itemCode))
        {
            return itemCode;
        }
        // 名称查询没有稳定编码时用 itemName 做记忆键，例如“卫衣”的库存快照。
        return arguments.getString("itemName");
    }

    private String buildSummary(String memoryKey, JSONObject result)
    {
        int total = result == null ? 0 : result.getIntValue("total");
        return "库存快照：" + memoryKey + "，命中 " + total + " 条库存记录";
    }

    private Date afterDays(int days)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }
}
