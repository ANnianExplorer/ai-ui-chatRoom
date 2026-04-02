package com.chat.core.common.config;

import com.chat.core.common.tools.MessageTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI 配置类
 * 统一通过 @Bean 注册 ChatClient，使用 Spring AI 标准方式管理模型交互。
 * 模型参数（api-key、model、temperature）由 spring.ai.zhipuai.* 配置项自动注入。
 *
 * @author y
 * @since 2026/4/2
 **/
@Configuration
public class AIConfig {

    /**
     * 会话记忆（滑动窗口实现），保留最近 50 条消息，以 conversationId 隔离每个用户会话。
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder().maxMessages(50).build();
    }

    /**
     * 通用对话客户端：携带会话记忆，无工具调用，用于所有角色扮演聊天。
     */
    @Bean("defaultChatClient")
    public ChatClient defaultChatClient(/*ZhiPuAiChatModel*/OllamaChatModel model, ChatMemory chatMemory) {
        return ChatClient.builder(model)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    /**
     * 智能代理客户端：MessageTool 工具集，用于 XXDLR 角色的 Function Calling。
     * 不挂载 MessageChatMemoryAdvisor —— Function Calling 每轮都会携带完整工具结果，
     * 若同时携带历史记录会导致每次内部循环都重复发送大量 token，极易触发 429 速率限制。
     * userId 通过 MessageTool.setCurrentUserId(userId) 在同一线程中注入（ThreadLocal 安全）。
     */
    @Bean("agentChatClient")
    public ChatClient agentChatClient(OllamaChatModel model, MessageTool messageTool) {
        return ChatClient.builder(model)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultTools(messageTool)
                .build();
    }

    /**
     * 轻量单次客户端：无会话记忆、无工具，用于摘要生成、回复建议、消息审核等一次性任务。
     */
    @Bean("simpleChatClient")
    public ChatClient simpleChatClient(OllamaChatModel model) {
        return ChatClient.builder(model)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}
