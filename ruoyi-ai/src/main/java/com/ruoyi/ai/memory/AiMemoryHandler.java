package com.ruoyi.ai.memory;

import com.ruoyi.ai.domain.AiAgentMemory;
import com.ruoyi.ai.domain.AiChatRequest;
import com.ruoyi.ai.domain.AiToolTrace;

/**
 * 工具结果到 Agent 记忆的转换器。
 *
 * 后续新增客户、订单、待确认动作等记忆时，只需要新增一个 Handler 实现。
 */
public interface AiMemoryHandler
{
    boolean supports(AiToolTrace trace);

    AiAgentMemory buildMemory(AiChatRequest request, AiToolTrace trace);
}
