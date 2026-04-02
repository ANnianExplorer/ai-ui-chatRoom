package com.chat.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.core.common.constant.SecurityConstants;
import com.chat.core.common.enums.RolePromptEnum;
import com.chat.core.common.tools.MessageTool;
import com.chat.domain.dto.ai.AIRequestDTO;
import com.chat.domain.pojo.Message;
import com.chat.service.AIService;
import com.chat.service.MessageService;
import com.chat.core.common.util.SecurityHolderUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * AI 服务实现类
 * 基于 Spring AI ChatClient 完全重写，删除 OkHttp3 手写调用。
 *
 * 架构说明：
 * - 普通角色聊天（非 XXDLR）：使用 defaultChatClient + 响应式流式输出
 * - 代理角色（XXDLR）：使用 agentChatClient + CompletableFuture 同步调用，
 *   确保 ThreadLocal userId 与工具执行在同一线程中（Function Calling 安全）
 * - 摘要/建议/审核：使用 simpleChatClient 一次性阻塞调用
 *
 * @author y
 * @since 2026-01-07
 */
@Slf4j
@Service
public class AIServiceImpl implements AIService {

    /**
     * MessageChatMemoryAdvisor 用于绑定会话 ID 的参数 key（Spring AI 1.1.0 中常量路径已变更，直接使用字面量）。
     */
    private static final String CONVERSATION_ID_KEY = "chat_memory_conversation_id";

    private static final String DONE_MARK = "[DONE]";
    private static final String RATE_LIMIT_ERROR = "大模型调用异常：请求频率过高，请稍后重试";

    private static final long RATE_LIMIT_WINDOW = 60;
    private static final int RATE_LIMIT_MAX_REQUESTS = 20;
    private final AtomicLong lastRequestTime = new AtomicLong(0);
    private final AtomicLong requestCount = new AtomicLong(0);

    @Autowired
    @Qualifier("defaultChatClient")
    private ChatClient defaultChatClient;

    @Autowired
    @Qualifier("agentChatClient")
    private ChatClient agentChatClient;

    @Autowired
    @Qualifier("simpleChatClient")
    private ChatClient simpleChatClient;

    @Autowired
    private MessageService messageService;

    // ──────────────────────── 主对话接口 ────────────────────────

    @Override
    public SseEmitter send(AIRequestDTO dto) {
        Integer userId = SecurityHolderUtils.getUserId();
        String chatId = "user-" + userId + "-" + SecurityConstants.AI_USER_ID;

        if (!checkRateLimit()) {
            return buildErrorEmitter(RATE_LIMIT_ERROR, userId);
        }

        saveMessage(userId, userId, dto.getPrompt());

        boolean isAgent = "XXDLR".equals(dto.getRole());
        String systemPrompt = resolveSystemPrompt(dto.getRole());
        SseEmitter emitter = new SseEmitter(0L);

        if (isAgent) {
            // Agent 模式：
            // - CompletableFuture 开启独立线程，在该线程设置 ThreadLocal
            // - 必须使用 .call().content()（同步阻塞）而非 .stream()
            //   原因：.stream() 底层由 Reactor IO 调度器在另一个线程执行 @Tool 方法，
            //         导致 ThreadLocal 跨线程失效（userId 读取为 null）
            // - .call() 全部在同一线程内同步完成，ThreadLocal 贯穿整个 Function Calling 循环
            CompletableFuture.runAsync(() -> {
                MessageTool.setCurrentUserId(userId);
                try {
                    ChatClient.ChatClientRequestSpec spec = agentChatClient.prompt()
                            .user(dto.getPrompt());
                    if (systemPrompt != null && !systemPrompt.isBlank()) {
                        spec = spec.system(systemPrompt);
                    }
                    String response = spec.call().content();
                    if (response == null || response.isBlank()) {
                        response = "（AI未返回有效响应，请稍后重试）";
                    }
                    sendChunk(emitter, response);
                    saveMessage(userId, SecurityConstants.AI_USER_ID, response);
                    safeSendDone(emitter);
                } catch (Exception e) {
                    log.error("Agent 调用异常 userId={}", userId, e);
                    sendChunk(emitter, friendlyError(e));
                    safeSendDone(emitter);
                } finally {
                    MessageTool.clearCurrentUserId();
                }
            });
        } else {
            // 普通角色：响应式流式输出
            ChatClient.ChatClientRequestSpec spec = defaultChatClient.prompt()
                    .user(dto.getPrompt())
                    .advisors(a -> a.param(CONVERSATION_ID_KEY, chatId));
            if (systemPrompt != null && !systemPrompt.isBlank()) {
                spec = spec.system(systemPrompt);
            }

            StringBuilder fullContent = new StringBuilder();
            spec.stream().content().subscribe(
                    chunk -> {
                        fullContent.append(chunk);
                        sendChunk(emitter, chunk);
                    },
                    error -> {
                        log.error("AI 流式响应异常 userId={}", userId, error);
                        sendChunk(emitter, friendlyError(error));
                        safeComplete(emitter);
                    },
                    () -> {
                        String aiContent = fullContent.toString();
                        if (aiContent.isBlank()) aiContent = "（未获取到响应，请稍后重试）";
                        saveMessage(userId, SecurityConstants.AI_USER_ID, aiContent);
                        safeSendDone(emitter);
                    }
            );
        }

        return emitter;
    }

    // ──────────────────────── 群聊摘要（SSE 流式）────────────────────────

    @Override
    public SseEmitter summarizeChat(String chatId, Integer messageCount) {
        Page<Message> page = messageService.getMessagesByChatId(chatId, 1, messageCount);
        List<Message> messages = page.getRecords();

        StringBuilder chatHistory = new StringBuilder(
                "以下是群聊消息记录，请用简洁的中文生成摘要，突出重要信息和讨论结论：\n\n");
        for (Message msg : messages) {
            if ("text".equals(msg.getContentType()) && msg.getContent() != null) {
                chatHistory.append("[用户").append(msg.getSendId()).append("]: ")
                        .append(msg.getContent()).append("\n");
            }
        }

        SseEmitter emitter = new SseEmitter(0L);
        simpleChatClient.prompt()
                .user(chatHistory.toString())
                .stream()
                .content()
                .subscribe(
                        chunk -> sendChunk(emitter, chunk),
                        error -> {
                            log.error("摘要生成异常", error);
                            sendChunk(emitter, "摘要生成失败：" + error.getMessage());
                            safeComplete(emitter);
                        },
                        () -> safeSendDone(emitter)
                );
        return emitter;
    }

    // ──────────────────────── AI 回复建议（非流式）────────────────────────

    @Override
    public List<String> suggestReplies(String chatId, String lastMessage) {
        String prompt = "根据以下聊天消息，生成3条简短自然的中文回复建议，每条回复一行，不要编号：\n" + lastMessage;
        try {
            String response = simpleChatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            if (response != null && !response.isBlank()) {
                return Arrays.stream(response.split("\n"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .limit(3)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("AI回复建议生成失败", e);
        }
        return List.of("好的", "明白了", "收到！");
    }

    // ──────────────────────── 消息违规审核（非流式）────────────────────────

    @Override
    public boolean auditMessage(String content) {
        if (content == null || content.trim().isEmpty()) return false;
        try {
            String prompt = "请判断以下消息是否包含违规内容（色情、暴力、政治敏感、辱骂等），只回答'违规'或'正常'：\n" + content;
            String response = simpleChatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            return response != null && response.contains("违规");
        } catch (Exception e) {
            log.error("AI消息审核失败", e);
        }
        return false;
    }

    // ──────────────────────── 私有工具方法 ────────────────────────

    private boolean checkRateLimit() {
        long currentTime = System.currentTimeMillis() / 1000;
        long lastTime = lastRequestTime.get();
        if (currentTime - lastTime > RATE_LIMIT_WINDOW) {
            lastRequestTime.set(currentTime);
            requestCount.set(0);
        }
        return requestCount.incrementAndGet() <= RATE_LIMIT_MAX_REQUESTS;
    }

    private String resolveSystemPrompt(String role) {
        if (role == null || role.isBlank()) return null;
        try {
            return RolePromptEnum.getRolePrompt(role);
        } catch (Exception e) {
            log.warn("未找到角色提示词: {}", role);
            return null;
        }
    }

    private void saveMessage(Integer userId, Integer sendId, String content) {
        if (content == null || content.isBlank()) return;
        String chatId = "user-" + userId + "-" + SecurityConstants.AI_USER_ID;
        Message m = new Message();
        m.setChatId(chatId);
        m.setContent(content);
        m.setContentType("text");
        m.setSendId(sendId);
        m.setIsRead(1);
        m.setStatus(1);
        m.setIsDeleted(0);
        messageService.save(m);
    }

    /**
     * 将文本 chunk 包装为前端期望的 SSE JSON 格式并发送。
     * 前端解析路径：json.choices[0].delta.content
     */
    private void sendChunk(SseEmitter emitter, String chunk) {
        try {
            Map<String, Object> data = Map.of(
                    "choices", List.of(Map.of("delta", Map.of("content", chunk)))
            );
            emitter.send(SseEmitter.event().data(JSONUtil.toJsonStr(data)));
        } catch (IOException e) {
            log.warn("发送 SSE chunk 失败", e);
        }
    }

    private void safeSendDone(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().data(DONE_MARK));
            emitter.complete();
        } catch (Exception e) {
            log.warn("发送 SSE done 失败", e);
        }
    }

    private void safeComplete(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception ignored) {
        }
    }

    private SseEmitter buildErrorEmitter(String errorMsg, Integer userId) {
        SseEmitter emitter = new SseEmitter(0L);
        sendChunk(emitter, errorMsg);
        safeSendDone(emitter);
        saveMessage(userId, SecurityConstants.AI_USER_ID, errorMsg);
        return emitter;
    }

    /**
     * 将异常转为对用户友好的提示文字，对 429 速率限制单独提示。
     */
    private String friendlyError(Throwable e) {
        String msg = e.getMessage();
        if (msg != null && (msg.contains("429") || msg.contains("1302") || msg.contains("速率"))) {
            return "🚫 AI 接口调用过于频繁，已触发速率限制，请等待约 30 秒后再试。" +
                    "（提示：消息代理人每次查询会发起多次 AI 请求，建议减少连续操作）";
        }
        if (msg != null && msg.contains("timeout")) {
            return "⏱️ AI 响应超时，请稍后重试。";
        }
        return "AI 调用异常：" + (msg != null ? msg : e.getClass().getSimpleName());
    }
}
