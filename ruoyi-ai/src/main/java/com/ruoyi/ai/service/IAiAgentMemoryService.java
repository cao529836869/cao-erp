package com.ruoyi.ai.service;

import com.ruoyi.ai.domain.AiAgentMemory;
import com.ruoyi.ai.domain.AiChatRequest;
import com.ruoyi.ai.domain.AiToolTrace;

public interface IAiAgentMemoryService
{
    void rememberToolTrace(AiChatRequest request, AiToolTrace trace);

    AiAgentMemory selectLatestValidMemory(String operName, String memoryType, String memoryKey, String sessionId);
}
