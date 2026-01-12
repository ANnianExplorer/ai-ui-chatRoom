package com.chat.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.domain.pojo.Message;
import com.chat.domain.pojo.User;
import com.chat.service.MessageService;
import com.chat.service.UserService;
import com.chat.core.common.rersult.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员消息控制器
 *
 * @author y
 * @since 2026-01-09
 */
@RequestMapping("/admin/messages")
@RestController
public class AdminMessageController {

    @Autowired
    private MessageService messageService;
    
    @Autowired
    private UserService userService;

    /**
     * 获取群聊消息
     *
     * @param page     当前页码
     * @param pageSize 每页大小
     * @param keyword  搜索关键词
     * @param groupId  群聊ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 群聊消息列表
     */
    @GetMapping("/group")
    public Result<?> getGroupMessages(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                     @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize,
                                     @RequestParam(name = "keyword", required = false) String keyword,
                                     @RequestParam(name = "groupId", required = false) Integer groupId,
                                     @RequestParam(name = "startTime", required = false) String startTime,
                                     @RequestParam(name = "endTime", required = false) String endTime) {
        // 构建查询条件
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        
        // 根据规则：chatid如果包含group，说明就是群聊信息
        queryWrapper.like(Message::getChatId, "group");
        
        // 处理搜索关键词
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(Message::getContent, keyword);
        }
        
        // 处理群聊ID筛选
        if (groupId != null) {
            queryWrapper.eq(Message::getChatId, "group-" + groupId);
        }
        
        // 处理时间范围筛选
        addTimeRangeFilter(queryWrapper, startTime, endTime);
        
        // 执行分页查询并返回格式化结果
        return getMessagePageResult(queryWrapper, page, pageSize);
    }

    /**
     * 获取用户对话消息
     *
     * @param page     当前页码
     * @param pageSize 每页大小
     * @param keyword  搜索关键词
     * @param userId   用户ID
     * @return 用户对话消息列表
     */
    @GetMapping("/user")
    public Result<?> getUserMessages(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                    @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize,
                                    @RequestParam(name = "keyword", required = false) String keyword,
                                    @RequestParam(name = "userId", required = false) Integer userId) {
        // 构建查询条件
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        
        // 根据规则：user-*-*格式为用户对话，排除user-*-0（机器人对话）
        queryWrapper.like(Message::getChatId, "user-")
                   .notLike(Message::getChatId, "user-%-0");
        
        // 处理搜索关键词
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(Message::getContent, keyword);
        }
        
        // 处理用户ID筛选
        if (userId != null) {
            queryWrapper.eq(Message::getSendId, userId);
        }
        
        // 执行分页查询并返回格式化结果
        return getMessagePageResult(queryWrapper, page, pageSize);
    }

    /**
     * 获取机器人消息
     *
     * @param page     当前页码
     * @param pageSize 每页大小
     * @param keyword  搜索关键词
     * @return 机器人消息列表
     */
    @GetMapping("/robot")
    public Result<?> getRobotMessages(@RequestParam(name = "page", defaultValue = "1") Integer page,
                                     @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize,
                                     @RequestParam(name = "keyword", required = false) String keyword) {
        // 构建查询条件
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        
        // 根据规则：chatid为user-*-0格式说明是机器人对话
        queryWrapper.like(Message::getChatId, "user-%-0");
        
        // 处理搜索关键词
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(Message::getContent, keyword);
        }
        
        // 执行分页查询并返回格式化结果
        return getMessagePageResult(queryWrapper, page, pageSize);
    }

    /**
     * 删除消息
     *
     * @param id 消息ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteMessage(@PathVariable("id") Long id) {
        boolean deleted = messageService.removeById(id);
        return deleted ? Result.ok() : Result.fail();
    }
    
    /**
     * 根据chatId和sendId判断消息类型
     *
     * @param chatId 聊天ID
     * @return 消息类型：group(群聊), user(用户对话), robot(机器人对话)
     */
    private String getMessageType(String chatId) {
        if (chatId.contains("group")) {
            return "group";
        } else if (chatId.contains("user-")) {
            // 机器人对话：chatid包含user而且第二个数字是0
            if (chatId.matches("user-\\d+-0")) {
                return "robot";
            } else {
                return "user";
            }
        }
        return "unknown";
    }
    
    /**
     * 根据消息类型添加筛选条件
     *
     * @param queryWrapper 查询包装器
     * @param messageType 消息类型
     */
    private void addMessageTypeFilter(LambdaQueryWrapper<Message> queryWrapper, String messageType) {
        switch (messageType) {
            case "group":
                queryWrapper.like(Message::getChatId, "group");
                break;
            case "user":
                queryWrapper.like(Message::getChatId, "user-")
                           .notLike(Message::getChatId, "user-%-0");
                break;
            case "robot":
                queryWrapper.like(Message::getChatId, "user-%-0");
                break;
            default:
                break;
        }
    }
    
    /**
     * 处理时间范围筛选
     *
     * @param queryWrapper 查询包装器
     * @param startTime 开始时间
     * @param endTime 结束时间
     */
    private void addTimeRangeFilter(LambdaQueryWrapper<Message> queryWrapper, String startTime, String endTime) {
        if (startTime != null && !startTime.isEmpty()) {
            queryWrapper.ge(Message::getCreateTime, startTime);
        }
        if (endTime != null && !endTime.isEmpty()) {
            queryWrapper.le(Message::getCreateTime, endTime);
        }
    }
    
    /**
     * 执行分页查询并返回统一格式结果
     *
     * @param queryWrapper 查询包装器
     * @param page 当前页码
     * @param pageSize 每页大小
     * @return 分页查询结果
     */
    private Result<?> getMessagePageResult(LambdaQueryWrapper<Message> queryWrapper, Integer page, Integer pageSize) {
        // 执行分页查询
        Page<Message> messagePage = messageService.page(new Page<>(page, pageSize), queryWrapper);
        
        // 收集所有相关用户ID
        List<Integer> userIds = new ArrayList<>();
        for (Message message : messagePage.getRecords()) {
            userIds.add(message.getSendId());
            
            // 对于用户对话，收集接收者ID
            String chatId = message.getChatId();
            if (chatId.contains("user-") && !chatId.endsWith("-0")) {
                String[] parts = chatId.split("-");
                if (parts.length == 3) {
                    try {
                        int userId1 = Integer.parseInt(parts[1]);
                        int userId2 = Integer.parseInt(parts[2]);
                        if (!userIds.contains(userId1)) userIds.add(userId1);
                        if (!userIds.contains(userId2)) userIds.add(userId2);
                    } catch (NumberFormatException e) {
                        // 忽略格式错误
                    }
                }
            }
        }
        
        // 获取用户信息映射
        Map<Integer, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userService.listByIds(userIds);
            for (User user : users) {
                userMap.put(user.getId(), user);
            }
        }
        
        // 格式化消息数据
        List<Map<String, Object>> formattedMessages = new ArrayList<>();
        for (Message message : messagePage.getRecords()) {
            String chatId = message.getChatId();
            String messageType = getMessageType(chatId);
            
            Map<String, Object> formatted = new HashMap<>();
            formatted.put("id", message.getId());
            formatted.put("content", message.getContent());
            formatted.put("contentType", message.getContentType());
            formatted.put("sendTime", message.getCreateTime());
            
            // 添加发送者信息
            User sender = userMap.get(message.getSendId());
            int sendId = message.getSendId();
            String senderName = sendId == 0 ? "聊天机器人" : (sender != null ? sender.getUsername() : "聊天机器人");
            String senderAvatar = sender != null ? sender.getAvatar() : "";
            
            if ("group".equals(messageType)) {
                // 群聊消息格式化
                formatted.put("groupId", chatId.replace("group-", ""));
                formatted.put("groupName", chatId);
                formatted.put("sender", senderName);
                formatted.put("senderAvatar", senderAvatar);
            } else if ("user".equals(messageType)) {
                // 用户对话消息格式化
                formatted.put("fromUser", senderName);
                formatted.put("fromUserAvatar", senderAvatar);
                
                // 根据chatid提取接收者ID
                String[] parts = chatId.split("-");
                if (parts.length == 3) {
                    try {
                        int senderId = message.getSendId();
                        int userId1 = Integer.parseInt(parts[1]);
                        int userId2 = Integer.parseInt(parts[2]);
                        int receiverId = senderId == userId1 ? userId2 : userId1;
                        
                        User receiver = userMap.get(receiverId);
                        formatted.put("toUser", receiver != null ? receiver.getUsername() : "未知用户");
                        formatted.put("toUserAvatar", receiver != null ? receiver.getAvatar() : "");
                    } catch (NumberFormatException e) {
                        // 忽略格式错误
                        formatted.put("toUser", "未知用户");
                        formatted.put("toUserAvatar", "");
                    }
                }
            } else if ("robot".equals(messageType)) {
                // 机器人消息格式化
                formatted.put("userId", message.getSendId());
                formatted.put("userName", senderName);
                formatted.put("userAvatar", senderAvatar);
                formatted.put("userMessage", message.getContent());
                formatted.put("robotResponse", ""); // 机器人回复需要从其他地方获取
                formatted.put("sessionId", chatId);
            }
            
            formattedMessages.add(formatted);
        }
        
        // 返回格式化后的结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", formattedMessages);
        result.put("total", messagePage.getTotal());
        
        return Result.ok(result);
    }
}
