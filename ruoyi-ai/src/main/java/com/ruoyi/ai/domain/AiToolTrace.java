package com.ruoyi.ai.domain;

import com.alibaba.fastjson2.JSONObject;

/**
 * AI Agent 工具调用轨迹。
 *
 * 前端可以用它展示“模型调用了哪个 ERP 工具、传入了什么参数、返回了什么结果”，
 * 方便调试 Agent 的执行过程。
 */
public class AiToolTrace
{
    private String toolName;

    private JSONObject arguments;

    private JSONObject result;

    public String getToolName()
    {
        return toolName;
    }

    public void setToolName(String toolName)
    {
        this.toolName = toolName;
    }

    public JSONObject getArguments()
    {
        return arguments;
    }

    public void setArguments(JSONObject arguments)
    {
        this.arguments = arguments;
    }

    public JSONObject getResult()
    {
        return result;
    }

    public void setResult(JSONObject result)
    {
        this.result = result;
    }
}
