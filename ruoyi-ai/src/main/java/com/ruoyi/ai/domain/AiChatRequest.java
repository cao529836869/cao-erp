package com.ruoyi.ai.domain;

import jakarta.validation.constraints.NotBlank;

public class AiChatRequest
{
    @NotBlank(message = "问题不能为空")
    private String prompt;

    private String model;

    private Boolean ragEnabled;

    private Boolean agentEnabled;

    private String systemPrompt;

    private String sessionId;

    private String operName;

    public String getPrompt()
    {
        return prompt;
    }

    public void setPrompt(String prompt)
    {
        this.prompt = prompt;
    }

    public String getModel()
    {
        return model;
    }

    public void setModel(String model)
    {
        this.model = model;
    }

    public Boolean getRagEnabled()
    {
        return ragEnabled;
    }

    public void setRagEnabled(Boolean ragEnabled)
    {
        this.ragEnabled = ragEnabled;
    }

    public Boolean getAgentEnabled()
    {
        return agentEnabled;
    }

    public void setAgentEnabled(Boolean agentEnabled)
    {
        this.agentEnabled = agentEnabled;
    }

    public String getSystemPrompt()
    {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt)
    {
        this.systemPrompt = systemPrompt;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(String sessionId)
    {
        this.sessionId = sessionId;
    }

    public String getOperName()
    {
        return operName;
    }

    public void setOperName(String operName)
    {
        this.operName = operName;
    }
}
