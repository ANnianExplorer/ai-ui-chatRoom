package com.chat.service;

import com.chat.domain.dto.ai.AIRequestDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * @author y
 * @since 2026-01-07
 */
public interface AIService {

    /**
     * 发送消息
     */
    SseEmitter send(AIRequestDTO aiRequestDTO);

    /**
     * 群聊消息摘要（流式）
     */
    SseEmitter summarizeChat(String chatId, Integer messageCount);

    /**
     * 辅助回复建议
     */
    List<String> suggestReplies(String chatId, String lastMessage);

    /**
     * 审核消息内容
     * @return true=违规，false=正常
     */
    boolean auditMessage(String content);
}
