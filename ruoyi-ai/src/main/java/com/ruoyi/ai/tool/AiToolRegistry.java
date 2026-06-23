package com.ruoyi.ai.tool;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * Agent 工具注册表。
 *
 * Spring 会自动收集所有 AiTool 实现类。后续要扩展工具，只需要新增一个实现类，
 * 不需要改聊天主流程。
 */
@Component
public class AiToolRegistry
{
    private final Map<String, AiTool> tools = new LinkedHashMap<>();

    public AiToolRegistry(List<AiTool> toolList)
    {
        for (AiTool tool : toolList)
        {
            tools.put(tool.name(), tool);
        }
    }

    public boolean hasTools()
    {
        return !tools.isEmpty();
    }

    public Collection<AiTool> listTools()
    {
        return tools.values();
    }

    public AiTool getRequiredTool(String name)
    {
        if (StringUtils.isBlank(name) || !tools.containsKey(name))
        {
            throw new ServiceException("AI Agent 请求了不存在的工具: " + name);
        }
        return tools.get(name);
    }

    public JSONArray describeTools()
    {
        JSONArray array = new JSONArray();
        for (AiTool tool : tools.values())
        {
            JSONObject item = new JSONObject();
            item.put("name", tool.name());
            item.put("description", tool.description());
            item.put("parameters", tool.schema());
            array.add(item);
        }
        return array;
    }
}
