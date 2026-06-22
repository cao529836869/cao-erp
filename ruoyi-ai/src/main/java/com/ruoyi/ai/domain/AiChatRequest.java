package com.ruoyi.ai.domain;

import jakarta.validation.constraints.NotBlank;

public class AiChatRequest
{
    @NotBlank(message = "问题不能为空")
    private String prompt;

    private String model;

    private Boolean ragEnabled;

    private String systemPrompt;

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

    public String getSystemPrompt()
    {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt)
    {
        this.systemPrompt = systemPrompt;
    }
}
