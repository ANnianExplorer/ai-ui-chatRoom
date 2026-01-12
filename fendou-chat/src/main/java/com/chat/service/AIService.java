package com.chat.service;

import com.chat.domain.dto.ai.AIRequestDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author y
 * @since 2026-01-07
 */
public interface AIService {

    /**
     * 发送消息
     *
     * @param aiRequestDTO 请求数据
     * @return SseEmitter
     */
    SseEmitter send(AIRequestDTO aiRequestDTO);
}
