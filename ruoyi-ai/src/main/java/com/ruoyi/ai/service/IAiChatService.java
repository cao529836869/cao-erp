package com.ruoyi.ai.service;

import java.util.List;

import com.ruoyi.ai.domain.AiChatRequest;
import com.ruoyi.ai.domain.AiChatResponse;

public interface IAiChatService
{
    AiChatResponse chat(AiChatRequest request);

    List<String> listModels();
}
