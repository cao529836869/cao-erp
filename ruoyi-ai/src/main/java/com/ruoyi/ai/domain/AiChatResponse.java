package com.ruoyi.ai.domain;

import java.util.List;

public class AiChatResponse
{
    private String model;

    private String content;

    private Long totalDuration;

    private Integer promptEvalCount;

    private Integer evalCount;

    private List<AiKnowledgeHit> references;

    public String getModel()
    {
        return model;
    }

    public void setModel(String model)
    {
        this.model = model;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public Long getTotalDuration()
    {
        return totalDuration;
    }

    public void setTotalDuration(Long totalDuration)
    {
        this.totalDuration = totalDuration;
    }

    public Integer getPromptEvalCount()
    {
        return promptEvalCount;
    }

    public void setPromptEvalCount(Integer promptEvalCount)
    {
        this.promptEvalCount = promptEvalCount;
    }

    public Integer getEvalCount()
    {
        return evalCount;
    }

    public void setEvalCount(Integer evalCount)
    {
        this.evalCount = evalCount;
    }

    public List<AiKnowledgeHit> getReferences()
    {
        return references;
    }

    public void setReferences(List<AiKnowledgeHit> references)
    {
        this.references = references;
    }
}
