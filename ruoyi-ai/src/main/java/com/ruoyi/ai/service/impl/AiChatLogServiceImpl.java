package com.ruoyi.ai.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ruoyi.ai.domain.AiChatLog;
import com.ruoyi.ai.mapper.AiChatLogMapper;
import com.ruoyi.ai.service.IAiChatLogService;

@Service
public class AiChatLogServiceImpl implements IAiChatLogService
{
    @Autowired
    private AiChatLogMapper chatLogMapper;

    @Override
    public List<AiChatLog> selectChatLogList(AiChatLog chatLog)
    {
        return chatLogMapper.selectChatLogList(chatLog);
    }

    @Override
    public AiChatLog selectChatLogById(Long chatLogId)
    {
        return chatLogMapper.selectChatLogById(chatLogId);
    }

    @Override
    public int insertChatLog(AiChatLog chatLog)
    {
        return chatLogMapper.insertChatLog(chatLog);
    }
}
