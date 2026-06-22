package com.ruoyi.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ai.ollama")
public class OllamaProperties
{
    private boolean enabled = true;

    private String baseUrl = "http://127.0.0.1:11434";

    private String model = "qwen";

    private String embeddingModel = "bge-m3";

    private boolean ragEnabled = true;

    private int ragTopK = 5;

    private double ragMinScore = 0.2D;

    private int timeoutSeconds = 120;

    private String systemPrompt = "你是一丫一 ERP 系统中的智能助手，请用简洁、准确的中文回答用户问题。";

    private Double temperature = 0.3D;

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public String getModel()
    {
        return model;
    }

    public void setModel(String model)
    {
        this.model = model;
    }

    public String getEmbeddingModel()
    {
        return embeddingModel;
    }

    public void setEmbeddingModel(String embeddingModel)
    {
        this.embeddingModel = embeddingModel;
    }

    public boolean isRagEnabled()
    {
        return ragEnabled;
    }

    public void setRagEnabled(boolean ragEnabled)
    {
        this.ragEnabled = ragEnabled;
    }

    public int getRagTopK()
    {
        return ragTopK;
    }

    public void setRagTopK(int ragTopK)
    {
        this.ragTopK = ragTopK;
    }

    public double getRagMinScore()
    {
        return ragMinScore;
    }

    public void setRagMinScore(double ragMinScore)
    {
        this.ragMinScore = ragMinScore;
    }

    public int getTimeoutSeconds()
    {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds)
    {
        this.timeoutSeconds = timeoutSeconds;
    }

    public String getSystemPrompt()
    {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt)
    {
        this.systemPrompt = systemPrompt;
    }

    public Double getTemperature()
    {
        return temperature;
    }

    public void setTemperature(Double temperature)
    {
        this.temperature = temperature;
    }
}
