package com.ruoyi.ai.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ruoyi.ai.config.OllamaProperties;
import com.ruoyi.ai.domain.AiKnowledgeChunk;
import com.ruoyi.ai.mapper.AiKnowledgeChunkMapper;
import com.ruoyi.ai.service.IAiEmbeddingService;
import com.ruoyi.ai.service.IAiKnowledgeService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class AiKnowledgeServiceImpl implements IAiKnowledgeService
{
    @Autowired
    private AiKnowledgeChunkMapper chunkMapper;

    @Autowired
    private IAiEmbeddingService embeddingService;

    @Autowired
    private OllamaProperties properties;

    @Override
    public List<AiKnowledgeChunk> selectKnowledgeChunkList(AiKnowledgeChunk chunk)
    {
        return chunkMapper.selectKnowledgeChunkList(chunk);
    }

    @Override
    public List<AiKnowledgeChunk> selectEnabledKnowledgeChunks(String embeddingModel)
    {
        return chunkMapper.selectEnabledKnowledgeChunks(embeddingModel);
    }

    @Override
    public AiKnowledgeChunk selectKnowledgeChunkById(Long chunkId)
    {
        return chunkMapper.selectKnowledgeChunkById(chunkId);
    }

    @Override
    public int insertKnowledgeChunk(AiKnowledgeChunk chunk)
    {
        fillEmbedding(chunk);
        if (StringUtils.isBlank(chunk.getStatus()))
        {
            chunk.setStatus("0");
        }
        return chunkMapper.insertKnowledgeChunk(chunk);
    }

    @Override
    public int updateKnowledgeChunk(AiKnowledgeChunk chunk)
    {
        fillEmbedding(chunk);
        return chunkMapper.updateKnowledgeChunk(chunk);
    }

    @Override
    public int deleteKnowledgeChunkByIds(Long[] chunkIds)
    {
        return chunkMapper.deleteKnowledgeChunkByIds(chunkIds);
    }

    @Override
    public int rebuildEmbedding(Long chunkId)
    {
        AiKnowledgeChunk chunk = selectKnowledgeChunkById(chunkId);
        if (chunk == null)
        {
            throw new ServiceException("知识片段不存在");
        }
        fillEmbedding(chunk);
        return chunkMapper.updateKnowledgeChunk(chunk);
    }

    private void fillEmbedding(AiKnowledgeChunk chunk)
    {
        if (StringUtils.isBlank(chunk.getContent()))
        {
            throw new ServiceException("知识内容不能为空");
        }
        chunk.setEmbeddingModel(properties.getEmbeddingModel());
        chunk.setEmbeddingJson(embeddingService.embedAsJson(buildEmbeddingText(chunk)));
    }

    private String buildEmbeddingText(AiKnowledgeChunk chunk)
    {
        StringBuilder text = new StringBuilder();
        appendLine(text, "标题", chunk.getTitle());
        appendLine(text, "模块", chunk.getModuleName());
        appendLine(text, "来源", chunk.getSourceName());
        text.append(chunk.getContent());
        return text.toString();
    }

    private void appendLine(StringBuilder text, String label, String value)
    {
        if (StringUtils.isNotBlank(value))
        {
            text.append(label).append(": ").append(value).append("\n");
        }
    }
}
