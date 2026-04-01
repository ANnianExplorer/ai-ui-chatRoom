package com.chat.controller;

import com.chat.core.common.rersult.Result;
import com.chat.core.common.util.SecurityHolderUtils;
import com.chat.domain.dto.ai.AIRequestDTO;
import com.chat.service.AIService;
import com.chat.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * @author y
 * @since 2026-01-07
 */

@Slf4j
@RestController
@RequestMapping("/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @Autowired
    private MessageService messageService;

    @RequestMapping(value = "/send", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody AIRequestDTO aiRequestDTO) {
        return aiService.send(aiRequestDTO);
    }

    /**
     * AI群聊消息摘要（SSE流式返回）
     */
    @RequestMapping(value = "/summary", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter summarizeChat(@RequestBody Map<String, Object> body) {
        String chatId = (String) body.get("chatId");
        Integer count = body.get("count") != null ? Integer.valueOf(body.get("count").toString()) : 20;
        return aiService.summarizeChat(chatId, count);
    }

    /**
     * AI辅助回复建议（返回3条候选）
     */
    @PostMapping("/suggest")
    public Result<?> suggestReply(@RequestBody Map<String, Object> body) {
        String chatId = (String) body.get("chatId");
        String lastMessage = (String) body.get("lastMessage");
        return Result.ok(aiService.suggestReplies(chatId, lastMessage));
    }

    /**
     * AI违规消息审核
     */
    @PostMapping("/audit")
    public Result<?> auditMessage(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        return Result.ok(aiService.auditMessage(content));
    }
}
