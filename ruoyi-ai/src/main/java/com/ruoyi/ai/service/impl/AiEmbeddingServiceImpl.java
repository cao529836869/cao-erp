package com.ruoyi.ai.service.impl;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.ai.config.OllamaProperties;
import com.ruoyi.ai.service.IAiEmbeddingService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class AiEmbeddingServiceImpl implements IAiEmbeddingService
{
    private final OllamaProperties properties;

    private final HttpClient httpClient;

    public AiEmbeddingServiceImpl(OllamaProperties properties)
    {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .build();
    }

    @Override
    public List<Double> embed(String input)
    {
        if (!properties.isEnabled())
        {
            throw new ServiceException("AI 服务未启用");
        }
        if (StringUtils.isBlank(input))
        {
            throw new ServiceException("向量化内容不能为空");
        }

        JSONObject body = new JSONObject();
        body.put("model", properties.getEmbeddingModel());
        body.put("input", input);

        try
        {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(normalizeBaseUrl(properties.getBaseUrl()) + "/api/embed"))
                    .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300)
            {
                throw new ServiceException("Ollama 向量化失败: HTTP " + response.statusCode() + " " + abbreviate(response.body()));
            }
            return parseEmbedding(response.body());
        }
        catch (IOException e)
        {
            throw new ServiceException("无法连接 Ollama 向量服务: " + e.getMessage());
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new ServiceException("Ollama 向量化被中断");
        }
    }

    @Override
    public String embedAsJson(String input)
    {
        return JSON.toJSONString(embed(input));
    }

    private List<Double> parseEmbedding(String body)
    {
        JSONObject json = JSON.parseObject(body);
        JSONArray embeddings = json.getJSONArray("embeddings");
        if (embeddings == null || embeddings.isEmpty())
        {
            throw new ServiceException("Ollama 向量返回为空");
        }

        JSONArray vector = embeddings.getJSONArray(0);
        List<Double> result = new ArrayList<>();
        for (int i = 0; i < vector.size(); i++)
        {
            result.add(vector.getDouble(i));
        }
        return result;
    }

    private String normalizeBaseUrl(String baseUrl)
    {
        String normalized = StringUtils.defaultIfBlank(baseUrl, "http://127.0.0.1:11434").trim();
        while (normalized.endsWith("/"))
        {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String abbreviate(String text)
    {
        if (StringUtils.isBlank(text) || text.length() <= 300)
        {
            return text;
        }
        return text.substring(0, 300) + "...";
    }
}
