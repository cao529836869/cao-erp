package com.ruoyi.ai.mapper;

import java.util.List;

import com.ruoyi.ai.domain.AiKnowledgeChunk;

public interface AiKnowledgeChunkMapper
{
    public List<AiKnowledgeChunk> selectKnowledgeChunkList(AiKnowledgeChunk chunk);

    public List<AiKnowledgeChunk> selectEnabledKnowledgeChunks(String embeddingModel);

    public AiKnowledgeChunk selectKnowledgeChunkById(Long chunkId);

    public int insertKnowledgeChunk(AiKnowledgeChunk chunk);

    public int updateKnowledgeChunk(AiKnowledgeChunk chunk);

    public int deleteKnowledgeChunkByIds(Long[] chunkIds);
}
