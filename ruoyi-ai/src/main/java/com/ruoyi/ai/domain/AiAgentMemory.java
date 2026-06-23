package com.ruoyi.ai.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI Agent 结构化记忆。
 *
 * 它和 ai_chat_log 的职责不同：chat_log 是审计日志，memory 是 Agent 后续可以读取、
 * 对比和推理的业务上下文。
 */
public class AiAgentMemory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long memoryId;

    private String memoryType;

    private String memoryKey;

    private String memoryTitle;

    private String toolName;

    private String argumentsJson;

    private String resultJson;

    private String summary;

    private String sessionId;

    private String operName;

    private Long sourceChatLogId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    private String status;

    public Long getMemoryId()
    {
        return memoryId;
    }

    public void setMemoryId(Long memoryId)
    {
        this.memoryId = memoryId;
    }

    public String getMemoryType()
    {
        return memoryType;
    }

    public void setMemoryType(String memoryType)
    {
        this.memoryType = memoryType;
    }

    public String getMemoryKey()
    {
        return memoryKey;
    }

    public void setMemoryKey(String memoryKey)
    {
        this.memoryKey = memoryKey;
    }

    public String getMemoryTitle()
    {
        return memoryTitle;
    }

    public void setMemoryTitle(String memoryTitle)
    {
        this.memoryTitle = memoryTitle;
    }

    public String getToolName()
    {
        return toolName;
    }

    public void setToolName(String toolName)
    {
        this.toolName = toolName;
    }

    public String getArgumentsJson()
    {
        return argumentsJson;
    }

    public void setArgumentsJson(String argumentsJson)
    {
        this.argumentsJson = argumentsJson;
    }

    public String getResultJson()
    {
        return resultJson;
    }

    public void setResultJson(String resultJson)
    {
        this.resultJson = resultJson;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
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

    public Long getSourceChatLogId()
    {
        return sourceChatLogId;
    }

    public void setSourceChatLogId(Long sourceChatLogId)
    {
        this.sourceChatLogId = sourceChatLogId;
    }

    public Date getExpireTime()
    {
        return expireTime;
    }

    public void setExpireTime(Date expireTime)
    {
        this.expireTime = expireTime;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}
