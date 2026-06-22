package com.ruoyi.ai.service;

import java.util.List;

import com.ruoyi.ai.domain.AiKnowledgeChunk;

public interface IAiKnowledgeService
{
    List<AiKnowledgeChunk> selectKnowledgeChunkList(AiKnowledgeChunk chunk);

    List<AiKnowledgeChunk> selectEnabledKnowledgeChunks(String embeddingModel);

    AiKnowledgeChunk selectKnowledgeChunkById(Long chunkId);

    int insertKnowledgeChunk(AiKnowledgeChunk chunk);

    int updateKnowledgeChunk(AiKnowledgeChunk chunk);

    int deleteKnowledgeChunkByIds(Long[] chunkIds);

    int rebuildEmbedding(Long chunkId);
}
