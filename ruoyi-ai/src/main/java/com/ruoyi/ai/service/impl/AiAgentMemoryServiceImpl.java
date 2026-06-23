package com.ruoyi.ai.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ruoyi.ai.domain.AiAgentMemory;
import com.ruoyi.ai.domain.AiChatRequest;
import com.ruoyi.ai.domain.AiToolTrace;
import com.ruoyi.ai.mapper.AiAgentMemoryMapper;
import com.ruoyi.ai.memory.AiMemoryHandler;
import com.ruoyi.ai.service.IAiAgentMemoryService;
import com.ruoyi.common.utils.StringUtils;

@Service
public class AiAgentMemoryServiceImpl implements IAiAgentMemoryService
{
    @Autowired
    private AiAgentMemoryMapper memoryMapper;

    @Autowired
    private List<AiMemoryHandler> memoryHandlers;

    @Override
    public void rememberToolTrace(AiChatRequest request, AiToolTrace trace)
    {
        // 记忆按用户隔离；没有 operName 时不保存，避免生成无法归属或跨用户可见的上下文。
        if (request == null || StringUtils.isBlank(request.getOperName()))
        {
            return;
        }
        for (AiMemoryHandler handler : memoryHandlers)
        {
            if (handler.supports(trace))
            {
                // 每类工具结果由对应 Handler 决定是否可记忆、记忆键是什么、多久过期。
                AiAgentMemory memory = handler.buildMemory(request, trace);
                if (memory != null && StringUtils.isNotBlank(memory.getMemoryKey()))
                {
                    memoryMapper.insertAgentMemory(memory);
                }
                return;
            }
        }
    }

    @Override
    public AiAgentMemory selectLatestValidMemory(String operName, String memoryType, String memoryKey, String sessionId)
    {
        // memoryKey 允许为空：用于“刚才/上次查询”这类引用式问题，取最近一条同类型记忆。
        AiAgentMemory query = new AiAgentMemory();
        query.setOperName(operName);
        query.setMemoryType(memoryType);
        query.setMemoryKey(memoryKey);
        query.setSessionId(sessionId);
        return memoryMapper.selectLatestValidMemory(query);
    }
}
