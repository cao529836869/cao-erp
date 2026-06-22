package com.ruoyi.ai.mapper;

import java.util.List;

import com.ruoyi.ai.domain.AiChatLog;

public interface AiChatLogMapper
{
    public List<AiChatLog> selectChatLogList(AiChatLog chatLog);

    public AiChatLog selectChatLogById(Long chatLogId);

    public int insertChatLog(AiChatLog chatLog);
}
