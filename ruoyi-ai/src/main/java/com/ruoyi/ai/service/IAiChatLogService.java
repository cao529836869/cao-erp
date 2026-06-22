package com.ruoyi.ai.service;

import java.util.List;

import com.ruoyi.ai.domain.AiChatLog;

public interface IAiChatLogService
{
    List<AiChatLog> selectChatLogList(AiChatLog chatLog);

    AiChatLog selectChatLogById(Long chatLogId);

    int insertChatLog(AiChatLog chatLog);
}
