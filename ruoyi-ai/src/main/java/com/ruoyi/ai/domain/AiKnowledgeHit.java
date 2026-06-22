package com.ruoyi.ai.domain;

public class AiKnowledgeHit
{
    private Long chunkId;

    private String title;

    private String moduleName;

    private String sourceName;

    private String content;

    private Double score;

    public Long getChunkId()
    {
        return chunkId;
    }

    public void setChunkId(Long chunkId)
    {
        this.chunkId = chunkId;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getModuleName()
    {
        return moduleName;
    }

    public void setModuleName(String moduleName)
    {
        this.moduleName = moduleName;
    }

    public String getSourceName()
    {
        return sourceName;
    }

    public void setSourceName(String sourceName)
    {
        this.sourceName = sourceName;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public Double getScore()
    {
        return score;
    }

    public void setScore(Double score)
    {
        this.score = score;
    }
}
