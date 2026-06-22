package com.ruoyi.ai.service;

import java.util.List;

import com.ruoyi.ai.domain.AiKnowledgeHit;

public interface IAiRagService
{
    List<AiKnowledgeHit> retrieve(String question);
}
