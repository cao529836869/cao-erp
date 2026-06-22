package com.ruoyi.ai.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class AiKnowledgeChunk extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long chunkId;

    @Excel(name = "标题")
    private String title;

    @Excel(name = "模块")
    private String moduleName;

    @Excel(name = "来源类型")
    private String sourceType;

    @Excel(name = "来源名称")
    private String sourceName;

    private String content;

    private String embeddingModel;

    private String embeddingJson;

    @Excel(name = "状态", readConverterExp = "0=启用,1=停用")
    private String status;

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

    public String getSourceType()
    {
        return sourceType;
    }

    public void setSourceType(String sourceType)
    {
        this.sourceType = sourceType;
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

    public String getEmbeddingModel()
    {
        return embeddingModel;
    }

    public void setEmbeddingModel(String embeddingModel)
    {
        this.embeddingModel = embeddingModel;
    }

    public String getEmbeddingJson()
    {
        return embeddingJson;
    }

    public void setEmbeddingJson(String embeddingJson)
    {
        this.embeddingJson = embeddingJson;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("chunkId", getChunkId())
            .append("title", getTitle())
            .append("moduleName", getModuleName())
            .append("sourceType", getSourceType())
            .append("sourceName", getSourceName())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .toString();
    }
}
