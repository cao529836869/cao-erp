package com.ruoyi.ai.mapper;

import com.ruoyi.ai.domain.AiAgentMemory;

public interface AiAgentMemoryMapper
{
    public int insertAgentMemory(AiAgentMemory memory);

    public AiAgentMemory selectLatestValidMemory(AiAgentMemory memory);
}
