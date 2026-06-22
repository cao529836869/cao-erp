package com.ruoyi.ai.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.ai.config.OllamaProperties;
import com.ruoyi.ai.domain.AiKnowledgeChunk;
import com.ruoyi.ai.domain.AiKnowledgeHit;
import com.ruoyi.ai.service.IAiEmbeddingService;
import com.ruoyi.ai.service.IAiKnowledgeService;
import com.ruoyi.ai.service.IAiRagService;
import com.ruoyi.common.utils.StringUtils;

@Service
public class AiRagServiceImpl implements IAiRagService
{
    @Autowired
    private IAiEmbeddingService embeddingService;

    @Autowired
    private IAiKnowledgeService knowledgeService;

    @Autowired
    private OllamaProperties properties;

    @Override
    public List<AiKnowledgeHit> retrieve(String question)
    {
        if (StringUtils.isBlank(question))
        {
            return new ArrayList<>();
        }

        List<Double> queryVector = embeddingService.embed(question);
        List<AiKnowledgeChunk> chunks = knowledgeService.selectEnabledKnowledgeChunks(properties.getEmbeddingModel());
        List<AiKnowledgeHit> hits = new ArrayList<>();
        for (AiKnowledgeChunk chunk : chunks)
        {
            if (StringUtils.isBlank(chunk.getEmbeddingJson()))
            {
                continue;
            }
            List<Double> chunkVector = JSON.parseArray(chunk.getEmbeddingJson(), Double.class);
            double score = cosine(queryVector, chunkVector);
            if (score >= properties.getRagMinScore())
            {
                hits.add(toHit(chunk, score));
            }
        }
        hits.sort(Comparator.comparing(AiKnowledgeHit::getScore).reversed());
        int topK = Math.max(1, properties.getRagTopK());
        return hits.size() > topK ? new ArrayList<>(hits.subList(0, topK)) : hits;
    }

    private AiKnowledgeHit toHit(AiKnowledgeChunk chunk, double score)
    {
        AiKnowledgeHit hit = new AiKnowledgeHit();
        hit.setChunkId(chunk.getChunkId());
        hit.setTitle(chunk.getTitle());
        hit.setModuleName(chunk.getModuleName());
        hit.setSourceName(chunk.getSourceName());
        hit.setContent(chunk.getContent());
        hit.setScore(score);
        return hit;
    }

    private double cosine(List<Double> left, List<Double> right)
    {
        int size = Math.min(left.size(), right.size());
        if (size == 0)
        {
            return 0D;
        }

        double dot = 0D;
        double leftNorm = 0D;
        double rightNorm = 0D;
        for (int i = 0; i < size; i++)
        {
            double a = left.get(i);
            double b = right.get(i);
            dot += a * b;
            leftNorm += a * a;
            rightNorm += b * b;
        }
        if (leftNorm == 0D || rightNorm == 0D)
        {
            return 0D;
        }
        return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }
}
