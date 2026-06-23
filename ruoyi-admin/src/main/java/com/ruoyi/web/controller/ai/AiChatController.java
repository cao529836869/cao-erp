package com.ruoyi.web.controller.ai;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.ai.domain.AiChatLog;
import com.ruoyi.ai.domain.AiChatRequest;
import com.ruoyi.ai.domain.AiChatResponse;
import com.ruoyi.ai.service.IAiChatLogService;
import com.ruoyi.ai.service.IAiChatService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;

@RestController
@RequestMapping("/ai")
public class AiChatController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(AiChatController.class);

    @Autowired
    private IAiChatService aiChatService;

    @Autowired
    private IAiChatLogService aiChatLogService;

    @PreAuthorize("@ss.hasPermi('ai:chat:use')")
    @GetMapping("/models")
    public AjaxResult models()
    {
        return success(aiChatService.listModels());
    }

    @PreAuthorize("@ss.hasPermi('ai:chat:use')")
    @PostMapping("/chat")
    public AjaxResult chat(@Valid @RequestBody AiChatRequest request)
    {
        request.setOperName(getUsername());
        AiChatLog chatLog = buildLog(request);
        try
        {
            AiChatResponse response = aiChatService.chat(request);
            chatLog.setModel(response.getModel());
            chatLog.setAnswer(response.getContent());
            chatLog.setMatchedChunks(JSON.toJSONString(response.getReferences()));
            chatLog.setStatus("0");
            saveChatLogQuietly(chatLog);
            response.setReferences(null);
            return success(response);
        }
        catch (RuntimeException e)
        {
            chatLog.setStatus("1");
            chatLog.setErrorMessage(abbreviate(e.getMessage(), 1000));
            saveChatLogQuietly(chatLog);
            throw e;
        }
    }

    private AiChatLog buildLog(AiChatRequest request)
    {
        AiChatLog chatLog = new AiChatLog();
        chatLog.setModel(request.getModel());
        chatLog.setPrompt(request.getPrompt());
        chatLog.setOperName(getUsername());
        return chatLog;
    }

    private void saveChatLogQuietly(AiChatLog chatLog)
    {
        try
        {
            aiChatLogService.insertChatLog(chatLog);
        }
        catch (RuntimeException e)
        {
            log.warn("AI chat log save failed: {}", e.getMessage());
        }
    }

    private String abbreviate(String text, int maxLength)
    {
        if (text == null || text.length() <= maxLength)
        {
            return text;
        }
        return text.substring(0, maxLength);
    }
}
