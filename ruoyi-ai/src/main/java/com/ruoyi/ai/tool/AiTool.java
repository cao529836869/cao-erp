package com.ruoyi.ai.tool;

import com.alibaba.fastjson2.JSONObject;

/**
 * ERP Agent 工具接口。
 *
 * 模型只能看到这里暴露的 name/description/schema，真正执行业务逻辑的仍然是后端。
 * 这样可以避免 AI 直接拼 SQL 或绕过系统权限、校验和事务边界。
 */
public interface AiTool
{
    /**
     * 工具名，要求稳定且唯一，模型会按这个名称发起调用。
     */
    String name();

    /**
     * 给模型看的工具说明，越具体越容易让模型正确选择工具。
     */
    String description();

    /**
     * 简化版参数说明。这里不追求完整 JSON Schema，只用于提示模型生成结构化参数。
     */
    JSONObject schema();

    /**
     * 执行工具。第一版只放只读工具，写入工具后续必须加二次确认。
     */
    JSONObject execute(JSONObject arguments);
}
