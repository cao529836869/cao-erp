package com.ruoyi.ai.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.ruoyi.common.core.domain.BaseEntity;

public class AiChatLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long chatLogId;

    private String model;

    private String prompt;

    private String answer;

    private String status;

    private String errorMessage;

    private String matchedChunks;

    private String operName;

    public Long getChatLogId()
    {
        return chatLogId;
    }

    public void setChatLogId(Long chatLogId)
    {
        this.chatLogId = chatLogId;
    }

    public String getModel()
    {
        return model;
    }

    public void setModel(String model)
    {
        this.model = model;
    }

    public String getPrompt()
    {
        return prompt;
    }

    public void setPrompt(String prompt)
    {
        this.prompt = prompt;
    }

    public String getAnswer()
    {
        return answer;
    }

    public void setAnswer(String answer)
    {
        this.answer = answer;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public String getMatchedChunks()
    {
        return matchedChunks;
    }

    public void setMatchedChunks(String matchedChunks)
    {
        this.matchedChunks = matchedChunks;
    }

    public String getOperName()
    {
        return operName;
    }

    public void setOperName(String operName)
    {
        this.operName = operName;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("chatLogId", getChatLogId())
            .append("model", getModel())
            .append("prompt", getPrompt())
            .append("answer", getAnswer())
            .append("status", getStatus())
            .append("errorMessage", getErrorMessage())
            .append("matchedChunks", getMatchedChunks())
            .append("operName", getOperName())
            .append("createTime", getCreateTime())
            .toString();
    }
}
