package com.chat.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * AI对话响应类
 *
 * @author y
 * @since 2026-01-07
 */

@Data
public class AIChatResponse {
    /**
     * 响应ID
     */
    private String id;
    /**
     * 模型名称
     */
    private String model;
    /**
     * 请求ID
     */
    private String request_id;
    /**
     * 选择项列表
     */
    private List<Choice> choices;
    /**
     * 使用情况
     */
    private Usage usage;
    /**
     * 创建时间
     */
    private long created;

    /**
     * 选择项类
     */
    @Data
    public static class Choice {
        private String finish_reason;
        private int index;
        private Delta delta;
    }

    /**
     * 消息类
     */
    @Data
    public static class Delta {
        private String content;
        private String reasoning_content;
        private String role;
    }

    /**
     * 使用情况类
     */
    @Data
    public static class Usage {
        private int completion_tokens;
        private int prompt_tokens;
        private PromptTokensDetails prompt_tokens_details;
        private int total_tokens;
    }

    /**
     * 提示令牌详情类
     */
    @Data
    public static class PromptTokensDetails {
        private int cached_tokens;
    }
}
