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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.ai.config.OllamaProperties;
import com.ruoyi.ai.domain.AiChatRequest;
import com.ruoyi.ai.domain.AiChatResponse;
import com.ruoyi.ai.domain.AiKnowledgeHit;
import com.ruoyi.ai.service.IAiChatService;
import com.ruoyi.ai.service.IAiRagService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class AiChatServiceImpl implements IAiChatService
{
    private final OllamaProperties properties;

    private final HttpClient httpClient;

    @Autowired
    private IAiRagService ragService;

    public AiChatServiceImpl(OllamaProperties properties)
    {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .build();
    }

    @Override
    public AiChatResponse chat(AiChatRequest request)
    {
        if (!properties.isEnabled())
        {
            throw new ServiceException("AI 服务未启用");
        }

        List<AiKnowledgeHit> references = shouldUseRag(request) ? ragService.retrieve(request.getPrompt()) : new ArrayList<>();

        JSONObject body = new JSONObject();
        body.put("model", getRequestModel(request));
        body.put("stream", false);
        body.put("messages", buildMessages(request, references));

        if (properties.getTemperature() != null)
        {
            JSONObject options = new JSONObject();
            options.put("temperature", properties.getTemperature());
            body.put("options", options);
        }

        try
        {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(normalizeBaseUrl(properties.getBaseUrl()) + "/api/chat"))
                    .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300)
            {
                throw new ServiceException("Ollama 调用失败: HTTP " + response.statusCode() + " " + abbreviate(response.body()));
            }
            AiChatResponse result = parseResponse(response.body());
            result.setReferences(references);
            return result;
        }
        catch (IOException e)
        {
            throw new ServiceException("无法连接 Ollama 服务: " + e.getMessage());
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new ServiceException("Ollama 调用被中断");
        }
    }

    @Override
    public List<String> listModels()
    {
        if (!properties.isEnabled())
        {
            throw new ServiceException("AI 服务未启用");
        }

        try
        {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(normalizeBaseUrl(properties.getBaseUrl()) + "/api/tags"))
                    .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300)
            {
                throw new ServiceException("Ollama 模型列表获取失败: HTTP " + response.statusCode() + " " + abbreviate(response.body()));
            }

            JSONObject json = JSON.parseObject(response.body());
            JSONArray models = json.getJSONArray("models");
            List<String> names = new ArrayList<>();
            addChatModel(names, properties.getModel());
            if (models != null)
            {
                for (int i = 0; i < models.size(); i++)
                {
                    JSONObject model = models.getJSONObject(i);
                    if (model != null && StringUtils.isNotBlank(model.getString("name")))
                    {
                        addChatModel(names, model.getString("name"));
                    }
                }
            }
            return names;
        }
        catch (IOException e)
        {
            throw new ServiceException("无法连接 Ollama 服务: " + e.getMessage());
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new ServiceException("Ollama 调用被中断");
        }
    }

    private JSONArray buildMessages(AiChatRequest request, List<AiKnowledgeHit> references)
    {
        JSONArray messages = new JSONArray();
        String systemPrompt = StringUtils.isNotBlank(request.getSystemPrompt()) ? request.getSystemPrompt() : properties.getSystemPrompt();
        if (StringUtils.isNotBlank(systemPrompt))
        {
            JSONObject system = new JSONObject();
            system.put("role", "system");
            system.put("content", systemPrompt);
            messages.add(system);
        }

        JSONObject user = new JSONObject();
        user.put("role", "user");
        user.put("content", buildUserPrompt(request.getPrompt(), references));
        messages.add(user);
        return messages;
    }

    private String buildUserPrompt(String prompt, List<AiKnowledgeHit> references)
    {
        if (references == null || references.isEmpty())
        {
            return prompt;
        }

        StringBuilder content = new StringBuilder();
        content.append("请优先依据下面的曹氏 ERP 知识库内容回答。");
        content.append("如果知识库没有足够依据，请明确说明，并给出需要进一步确认的信息。\n\n");
        content.append("回答时不要暴露、列出或提及“参考知识”“知识库片段”“来源标题”等内部检索信息，只输出面向用户的业务答案。\n\n");
        content.append("【ERP知识库】\n");
        for (int i = 0; i < references.size(); i++)
        {
            AiKnowledgeHit hit = references.get(i);
            content.append(i + 1).append(". ");
            if (StringUtils.isNotBlank(hit.getTitle()))
            {
                content.append(hit.getTitle());
            }
            if (StringUtils.isNotBlank(hit.getModuleName()))
            {
                content.append("（").append(hit.getModuleName()).append("）");
            }
            content.append("\n");
            content.append(hit.getContent()).append("\n\n");
        }
        content.append("【用户问题】\n").append(prompt);
        return content.toString();
    }

    private AiChatResponse parseResponse(String body)
    {
        JSONObject json = JSON.parseObject(body);
        JSONObject message = json.getJSONObject("message");
        if (message == null || StringUtils.isBlank(message.getString("content")))
        {
            throw new ServiceException("Ollama 返回内容为空");
        }

        AiChatResponse result = new AiChatResponse();
        result.setModel(json.getString("model"));
        result.setContent(message.getString("content"));
        result.setTotalDuration(json.getLong("total_duration"));
        result.setPromptEvalCount(json.getInteger("prompt_eval_count"));
        result.setEvalCount(json.getInteger("eval_count"));
        return result;
    }

    private String getRequestModel(AiChatRequest request)
    {
        String model = StringUtils.isNotBlank(request.getModel()) ? request.getModel() : properties.getModel();
        if (isEmbeddingModel(model))
        {
            throw new ServiceException("当前选择的是向量模型 " + model + "，不能用于对话。请切换为 " + properties.getModel() + " 等聊天模型。");
        }
        return model;
    }

    private void addChatModel(List<String> names, String model)
    {
        if (StringUtils.isBlank(model) || isEmbeddingModel(model) || names.contains(model))
        {
            return;
        }
        names.add(model);
    }

    private boolean isEmbeddingModel(String model)
    {
        return StringUtils.equals(normalizeModelName(model), normalizeModelName(properties.getEmbeddingModel()));
    }

    private String normalizeModelName(String model)
    {
        String normalized = StringUtils.defaultString(model).trim();
        if (normalized.endsWith(":latest"))
        {
            normalized = normalized.substring(0, normalized.length() - ":latest".length());
        }
        return normalized;
    }

    private boolean shouldUseRag(AiChatRequest request)
    {
        return request.getRagEnabled() != null ? request.getRagEnabled() : properties.isRagEnabled();
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
