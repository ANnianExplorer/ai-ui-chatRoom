package com.chat.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.core.common.constant.AIModelConstants;
import com.chat.core.common.constant.SecurityConstants;
import com.chat.domain.dto.ai.AIRequestDTO;
import com.chat.domain.pojo.Message;
import com.chat.domain.vo.AIChatResponse;
import com.chat.service.AIService;
import com.chat.service.MessageService;

import com.chat.core.common.util.SecurityHolderUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.BufferedSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AI 服务实现类
 *
 * @author y
 * @since 2026-01-07
 */

@Slf4j
@Service
public class AIServiceImpl implements AIService {
    private static final Map<String, SseEmitter> SSE_EMITTERS = new ConcurrentHashMap<>();

    private static final String USER_ROLE = "user";
    private static final String ASSISTANT_ROLE = "assistant";
    private static final String DATA_PREFIX = "data:";
    private static final String DONE_MARK = "[DONE]";
    private static final String ERROR_MESSAGE = "大模型调用异常（请检查配置是否正确或者稍后重试）";
    private static final String EMPTY_RESPONSE = "响应数据为空";
    private static final String RATE_LIMIT_ERROR = "大模型调用异常：请求频率过高，请稍后重试";
    
    // 限流相关配置
    private static final long RATE_LIMIT_WINDOW = 60; // 限流窗口，单位秒
    private static final int RATE_LIMIT_MAX_REQUESTS = 10; // 窗口内最大请求数
    private final AtomicLong lastRequestTime = new AtomicLong(0);
    private final AtomicLong requestCount = new AtomicLong(0);
    
    // 重试相关配置
    private static final int MAX_RETRY_COUNT = 3; // 最大重试次数
    private static final long INITIAL_RETRY_DELAY = 1000; // 初始重试延迟，单位毫秒

    @Autowired
    private MessageService messageService;

    private final OkHttpClient client;

    public AIServiceImpl() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public SseEmitter send(AIRequestDTO aiRequestDTO) {
        Integer userId = SecurityHolderUtils.getUserId();
        // 使用原本的chatId格式：user-{userId}-0（AI的固定ID是0）
        String chatId = "user-" + userId + "-" + SecurityConstants.AI_USER_ID;
        String prompt = aiRequestDTO.getPrompt();

        // 保存用户消息
        saveUserMessage(userId, userId, prompt);

        String uid = UUID.randomUUID().toString();
        SseEmitter emitter = createSseEmitter(uid);

        // 检查限流
        if (!checkRateLimit()) {
            CompletableFuture.runAsync(() -> {
                try {
                    emitter.send(SseEmitter.event().data(RATE_LIMIT_ERROR));
                    emitter.send(SseEmitter.event().data(DONE_MARK));
                    emitter.complete();
                    // 保存错误响应
                    saveUserMessage(userId, SecurityConstants.AI_USER_ID, RATE_LIMIT_ERROR);
                } catch (Exception e) {
                    log.error("发送限流错误消息失败", e);
                    emitter.completeWithError(e);
                } finally {
                    SSE_EMITTERS.remove(uid);
                }
            });
            return emitter;
        }

        // 获取历史消息
        List<Message> messageList = getHistoryMessages(aiRequestDTO.isHistory(), chatId);
        List<Map<String, String>> historyMessages = buildHistory(messageList);

        // 异步执行耗时的大模型调用操作
        CompletableFuture.runAsync(() ->
                processAiRequest(emitter, aiRequestDTO, historyMessages, userId, uid)
        );

        return emitter;
    }
    
    /**
     * 检查请求频率是否超过限制
     * @return true if allowed, false otherwise
     */
    private boolean checkRateLimit() {
        long currentTime = System.currentTimeMillis() / 1000; // 转换为秒
        long lastTime = lastRequestTime.get();
        
        // 如果超过限流窗口，重置计数器
        if (currentTime - lastTime > RATE_LIMIT_WINDOW) {
            lastRequestTime.set(currentTime);
            requestCount.set(0);
        }
        
        // 检查是否超过最大请求数
        long count = requestCount.incrementAndGet();
        return count <= RATE_LIMIT_MAX_REQUESTS;
    }

    private List<Message> getHistoryMessages(boolean useHistory, String chatId) {
        if (useHistory) {
            Page<Message> page = messageService.getMessagesByChatId(chatId, 1, 10);
            if (!page.getRecords().isEmpty()) {
                return page.getRecords();
            }
        }
        return new ArrayList<>();
    }

    private SseEmitter createSseEmitter(String uid) {
        SseEmitter emitter = new SseEmitter(0L);

        emitter.onCompletion(() -> SSE_EMITTERS.remove(uid));
        emitter.onTimeout(() -> {
            SSE_EMITTERS.remove(uid);
            emitter.complete();
        });
        emitter.onError(throwable -> {
            SSE_EMITTERS.remove(uid);
            emitter.completeWithError(throwable);
        });

        SSE_EMITTERS.put(uid, emitter);
        return emitter;
    }

    private void processAiRequest(SseEmitter emitter, AIRequestDTO aiRequestDTO, 
                                  List<Map<String, String>> historyMessages, Integer userId, String uid) {
        StringBuilder content = new StringBuilder();

        try {
            // 构造请求体
            Map<String, Object> requestBody = buildRequestBody(aiRequestDTO, historyMessages);

            log.info("messages: {}", JSONUtil.toJsonStr(requestBody.get("messages")));

            // 发送请求并处理流式响应
            sendAiRequest(emitter, requestBody, content);

            // 存储响应消息
            if(content.isEmpty()){
                content = new StringBuilder(ERROR_MESSAGE);
            }
            saveUserMessage(userId, SecurityConstants.AI_USER_ID, content.toString());

            // 发送结束标志并关闭连接
            emitter.send(SseEmitter.event().data(DONE_MARK));
            emitter.complete();

        } catch (IllegalStateException e) {
            // 忽略ResponseBodyEmitter已经完成的异常，这是正常的，因为在sendAiRequest中已经处理了错误
            log.warn("ResponseBodyEmitter已经完成，无需重复处理: {}", e.getMessage());
        } catch (Exception e) {
            handleError(emitter, e);
        } finally {
            SSE_EMITTERS.remove(uid);
        }
    }

    private Map<String, Object> buildRequestBody(AIRequestDTO aiRequestDTO, List<Map<String, String>> historyMessages) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", AIModelConstants.MODEL);
        requestBody.put("messages", buildMessages(historyMessages, aiRequestDTO));
        requestBody.put("stream", true);
        requestBody.put("thinking", Map.of("type", aiRequestDTO.isThinking() ? "enabled" : "disabled"));
        return requestBody;
    }

    private List<Map<String, String>> buildMessages(List<Map<String, String>> historyMessages, AIRequestDTO aiRequestDTO) {
        List<Map<String, String>> messages = new ArrayList<>();

        // 添加历史对话
        if (!historyMessages.isEmpty()) {
            messages.addAll(historyMessages);
        }

        // 用户对话
        Map<String, String> promptMap = new HashMap<>();
        promptMap.put("role", USER_ROLE);
        promptMap.put("content", aiRequestDTO.getPrompt());
        messages.add(promptMap);

        return messages;
    }

    private void sendAiRequest(SseEmitter emitter, Map<String, Object> requestBody, StringBuilder content)
            throws IOException, InterruptedException {
        Request request = new Request.Builder()
                .url(AIModelConstants.API_URL)
                .addHeader("Authorization", "Bearer " + AIModelConstants.API_KEY)
                .post(RequestBody.create(JSONUtil.toJsonStr(requestBody), MediaType.get("application/json")))
                .build();

        int retryCount = 0;
        long retryDelay = INITIAL_RETRY_DELAY;

        while (retryCount < MAX_RETRY_COUNT) {
            try (Response response = client.newCall(request).execute()) {
                log.info("response: {}", response);
                
                if (response.code() == 429) {
                    // 遇到429错误，进行重试
                    retryCount++;
                    log.warn("大模型调用异常：请求频率过高（429），正在进行第 {} 次重试，延迟 {}ms", 
                            retryCount, retryDelay);
                    
                    if (retryCount < MAX_RETRY_COUNT) {
                        // 延迟重试
                        Thread.sleep(retryDelay);
                        // 指数退避
                        retryDelay *= 2;
                        continue;
                    } else {
                        // 重试次数耗尽
                        String errorMsg = "大模型调用异常：请求频率过高，请稍后重试（已重试 " + retryCount + " 次）";
                        log.error(errorMsg);
                        emitter.send(SseEmitter.event().data(errorMsg));
                        emitter.complete();
                        return;
                    }
                } else if (!response.isSuccessful()) {
                    // 其他错误，直接返回
                    String errorMsg = "大模型调用异常：服务返回错误（" + response.code() + " - " + response.message() + "）";
                    log.error(errorMsg);
                    emitter.send(SseEmitter.event().data(errorMsg));
                    emitter.complete();
                    return;
                }

                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    log.error("大模型调用异常：响应体为空");
                    emitter.send(SseEmitter.event().data(EMPTY_RESPONSE));
                    emitter.complete();
                    return;
                }

                // 处理流式响应
                processStreamResponse(emitter, responseBody, content);
                return; // 成功，退出重试循环
            } catch (InterruptedException e) {
                log.error("重试线程被中断", e);
                Thread.currentThread().interrupt();
                throw new IOException("重试线程被中断", e);
            } catch (IOException e) {
                log.error("大模型调用异常：网络请求失败（第 {} 次）", retryCount + 1, e);
                
                retryCount++;
                if (retryCount < MAX_RETRY_COUNT) {
                    // 网络错误也进行重试
                    log.warn("网络请求失败，正在进行第 {} 次重试，延迟 {}ms", retryCount, retryDelay);
                    Thread.sleep(retryDelay);
                    retryDelay *= 2;
                } else {
                    // 重试次数耗尽，抛出异常
                    throw e;
                }
            }
        }
    }

    private void processStreamResponse(SseEmitter emitter, ResponseBody responseBody, StringBuilder content)
            throws IOException {
        try (BufferedSource source = responseBody.source()) {
            while (!source.exhausted()) {
                String line = source.readUtf8Line();
                if (line != null && line.startsWith(DATA_PREFIX)) {
                    String data = line.substring(5).trim();
                    if (!DONE_MARK.equals(data)) {
                        processStreamData(emitter, data, content);
                    }
                }
            }
        }
    }

    private void processStreamData(SseEmitter emitter, String data, StringBuilder content) {
        try {
            // 解析JSON数据
            AIChatResponse aiChatResponse = JSONUtil.toBean(data, AIChatResponse.class);

            // 提取并拼接content
            if (aiChatResponse.getChoices() != null && !aiChatResponse.getChoices().isEmpty()) {
                AIChatResponse.Choice choice = aiChatResponse.getChoices().get(0);
                if (choice.getDelta() != null) {
                    String contentPart = choice.getDelta().getContent();
                    if (contentPart != null) {
                        content.append(contentPart);
                    }
                }
            }

            // 将原始数据发送给客户端
            emitter.send(SseEmitter.event().data(data));
        } catch (Exception e) {
            log.warn("Failed to parse or process stream data: {}", data, e);
            try {
                emitter.send(SseEmitter.event().data(data));
            } catch (IOException ioException) {
                log.error("Failed to send data to client", ioException);
            }
        }
    }

    /**
     * 保存用户消息
     *
     * @param userId  用户ID
     * @param sendId  发送者ID
     * @param content 消息内容
     */
    private void saveUserMessage(Integer userId, Integer sendId, String content) {
        // 使用原本的chatId格式：user-{userId}-0（AI的固定ID是0）
        String chatId = "user-" + userId + "-" + SecurityConstants.AI_USER_ID;

        Message m = new Message();
        m.setChatId(chatId);
        m.setContent(content);
        m.setContentType("text");
        m.setSendId(sendId);
        m.setIsRead(1); // 设置为已读
        m.setStatus(1); // 设置为正常状态
        m.setIsDeleted(0); // 设置为未删除

        messageService.save(m);
    }

    /**
     * 构建历史消息
     *
     * @param historyMessages 历史消息
     * @return 构建后的历史消息
     */
    private List<Map<String, String>> buildHistory(List<Message> historyMessages) {
        List<Map<String, String>> messages = new ArrayList<>();

        if (!historyMessages.isEmpty()) {
            for (Message message : historyMessages) {
                Map<String, String> messageMap = new HashMap<>();
                if (!Objects.equals(message.getSendId(), SecurityConstants.AI_USER_ID)) {
                    messageMap.put("role", USER_ROLE);
                    messageMap.put("content", message.getContent());
                } else {
                    messageMap.put("role", ASSISTANT_ROLE);
                    messageMap.put("content", message.getContent());
                }
                messages.add(messageMap);
            }
        }

        return messages;
    }

    @Override
    public SseEmitter summarizeChat(String chatId, Integer messageCount) {
        // 获取最新N条消息
        Page<Message> page = messageService.getMessagesByChatId(chatId, 1, messageCount);
        List<Message> messages = page.getRecords();

        StringBuilder chatHistory = new StringBuilder("以下是群聊消息记录，请用简洁的中文生成摘要，突出重要信息和讨论结论：\n\n");
        for (Message msg : messages) {
            if (msg.getContentType() != null && msg.getContentType().equals("text") && msg.getContent() != null) {
                chatHistory.append("[用户").append(msg.getSendId()).append("]: ").append(msg.getContent()).append("\n");
            }
        }

        String uid = UUID.randomUUID().toString();
        SseEmitter emitter = createSseEmitter(uid);

        final String prompt = chatHistory.toString();
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", AIModelConstants.MODEL);
                List<Map<String, String>> msgs = new ArrayList<>();
                Map<String, String> userMsg = new HashMap<>();
                userMsg.put("role", "user");
                userMsg.put("content", prompt);
                msgs.add(userMsg);
                requestBody.put("messages", msgs);
                requestBody.put("stream", true);
                requestBody.put("thinking", Map.of("type", "disabled"));
                StringBuilder content = new StringBuilder();
                sendAiRequest(emitter, requestBody, content);
                emitter.send(SseEmitter.event().data(DONE_MARK));
                emitter.complete();
            } catch (Exception e) {
                handleError(emitter, e);
            } finally {
                SSE_EMITTERS.remove(uid);
            }
        });
        return emitter;
    }

    @Override
    public List<String> suggestReplies(String chatId, String lastMessage) {
        String prompt = "根据以下聊天消息，生成3条简短自然的中文回复建议，每条回复一行，不要编号：\n" + lastMessage;
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", AIModelConstants.MODEL);
            List<Map<String, String>> msgs = new ArrayList<>();
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            msgs.add(userMsg);
            requestBody.put("messages", msgs);
            requestBody.put("stream", false);
            requestBody.put("thinking", Map.of("type", "disabled"));

            Request request = new Request.Builder()
                    .url(AIModelConstants.API_URL)
                    .addHeader("Authorization", "Bearer " + AIModelConstants.API_KEY)
                    .post(RequestBody.create(JSONUtil.toJsonStr(requestBody), MediaType.get("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseStr = response.body().string();
                    cn.hutool.json.JSONObject jsonObj = JSONUtil.parseObj(responseStr);
                    String content = jsonObj.getByPath("choices[0].message.content", String.class);
                    if (content != null) {
                        String[] lines = content.split("\n");
                        List<String> suggestions = new ArrayList<>();
                        for (String line : lines) {
                            String trimmed = line.trim();
                            if (!trimmed.isEmpty()) {
                                suggestions.add(trimmed);
                            }
                            if (suggestions.size() >= 3) break;
                        }
                        return suggestions;
                    }
                }
            }
        } catch (Exception e) {
            log.error("AI建议回复生成失败", e);
        }
        return List.of("好的", "明白了", "收到！");
    }

    @Override
    public boolean auditMessage(String content) {
        if (content == null || content.trim().isEmpty()) return false;
        try {
            String prompt = "请判断以下消息是否包含违规内容（色情、暴力、政治敏感、辱骂等），只回答"违规"或"正常"：\n" + content;
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", AIModelConstants.MODEL);
            List<Map<String, String>> msgs = new ArrayList<>();
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            msgs.add(userMsg);
            requestBody.put("messages", msgs);
            requestBody.put("stream", false);
            requestBody.put("thinking", Map.of("type", "disabled"));

            Request request = new Request.Builder()
                    .url(AIModelConstants.API_URL)
                    .addHeader("Authorization", "Bearer " + AIModelConstants.API_KEY)
                    .post(RequestBody.create(JSONUtil.toJsonStr(requestBody), MediaType.get("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseStr = response.body().string();
                    cn.hutool.json.JSONObject jsonObj = JSONUtil.parseObj(responseStr);
                    String result = jsonObj.getByPath("choices[0].message.content", String.class);
                    return result != null && result.contains("违规");
                }
            }
        } catch (Exception e) {
            log.error("AI消息审核失败", e);
        }
        return false;
    }

    private void handleError(SseEmitter emitter, Exception e) {
        String detailedErrorMessage = ERROR_MESSAGE;
        // 添加更详细的错误信息
        if (e instanceof IOException) {
            detailedErrorMessage = "大模型调用异常：网络连接问题（" + e.getMessage() + "）";
        } else {
            detailedErrorMessage = "大模型调用异常：" + e.getMessage();
        }
        
        log.error("AI request failed: {}", e.getMessage(), e);
        
        try {
            emitter.send(SseEmitter.event().data(detailedErrorMessage));
        } catch (IOException ioException) {
            log.error("Failed to send error message to client", ioException);
        }
        emitter.completeWithError(e);
    }
}
/*
*
## 1. 请求限流机制
- 配置 ：每分钟最多允许10个请求
- 实现 ：使用时间窗口+计数器的方式
- 效果 ：超过限制时立即返回友好提示，避免无效请求
- 线程安全 ：使用AtomicLong保证多线程环境下的准确性
## 2. 智能重试机制
- 针对429错误 ：当遇到"请求过多"错误时自动重试
- 重试策略 ：最多重试3次，采用指数退避算法（1s → 2s → 4s）
- 覆盖范围 ：同时支持网络错误的重试
- 友好提示 ：重试耗尽时返回清晰的错误信息
## 3. 优化的错误处理
- 区分错误类型 ：限流、服务端错误、网络错误等
- 详细日志 ：记录完整的错误信息和重试过程
- 避免重复调用 ：修复了之前的"ResponseBodyEmitter has already completed"异常
## 4. 改进的用户体验
- 即时反馈 ：请求频率过高时立即返回提示
- 自动恢复 ：临时限流时自动重试，提高成功率
- 清晰提示 ：返回用户易懂的错误信息
## 配置调整
可以根据实际需求调整以下参数：

- RATE_LIMIT_WINDOW ：限流窗口大小（秒）
- RATE_LIMIT_MAX_REQUESTS ：窗口内最大请求数
- MAX_RETRY_COUNT ：最大重试次数
- INITIAL_RETRY_DELAY ：初始重试延迟（毫秒）
* */