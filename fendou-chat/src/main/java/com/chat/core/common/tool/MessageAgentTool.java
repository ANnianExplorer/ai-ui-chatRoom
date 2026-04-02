package com.chat.core.common.tool;

import cn.hutool.json.JSONUtil;
import com.chat.domain.pojo.Message;
import com.chat.domain.pojo.User;
import com.chat.domain.pojo.UserFriend;
import com.chat.mapper.UserFriendMapper;
import com.chat.mapper.UserMapper;
import com.chat.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class MessageAgentTool {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserFriendMapper userFriendMapper;

    @Autowired
    private MessageService messageService;

    public static final String TOOL_SEND_MESSAGE = "sendMessageToUser";
    public static final String TOOL_GET_FRIEND_LIST = "getFriendList";
    public static final String TOOL_SEARCH_USER = "searchUserByName";

    public String execute(String toolName, Map<String, Object> arguments, Integer userId) {
        log.info("执行工具: {}, 参数: {}, userId: {}", toolName, arguments, userId);

        if (userId == null) {
            return "错误：无法获取当前用户信息，请重新登录";
        }

        try {
            if (toolName == null) {
                return "错误：工具名称不能为空";
            }
            return switch (toolName) {
                case TOOL_SEND_MESSAGE -> sendMessageToUser(arguments, userId);
                case TOOL_GET_FRIEND_LIST -> getFriendList(userId);
                case TOOL_SEARCH_USER -> searchUserByName(arguments);
                default -> "未知工具: " + toolName;
            };
        } catch (Exception e) {
            log.error("工具执行异常: {}", e.getMessage(), e);
            return "工具执行失败: " + e.getMessage();
        }
    }

    public String sendMessageToUser(Map<String, Object> arguments, Integer userId) {
        String userName = (String) arguments.get("userName");
        String message = (String) arguments.get("message");

        if (userName == null || userName.trim().isEmpty()) {
            return "错误：请提供目标用户名（userName）";
        }
        if (message == null || message.trim().isEmpty()) {
            return "错误：请提供要发送的消息内容（message）";
        }

        UserFriend friend = findFriendByName(userId, userName);
        if (friend == null) {
            return "错误：未找到名为 '" + userName + "' 的好友，请确认用户名是否正确";
        }

        Integer targetUserId = friend.getFriendId();
        String chatId = buildChatId(userId, targetUserId);

        Message msg = new Message();
        msg.setChatId(chatId);
        msg.setContent(message);
        msg.setContentType("text");
        msg.setSendId(userId);
        msg.setIsRead(0);
        msg.setStatus(1);
        msg.setIsDeleted(0);

        try {
            messageService.savePrivateMessage(msg, userId, targetUserId);
            User targetUser = userMapper.selectById(targetUserId);
            String targetName = targetUser != null ? targetUser.getRealName() : "用户";
            return "SUCCESS: 消息已成功发送给 " + targetName + "（" + message + "）";
        } catch (Exception e) {
            log.error("发送消息失败", e);
            return "错误：消息发送失败，" + e.getMessage();
        }
    }

    public String getFriendList(Integer userId) {
        if (userId == null) {
            return "错误：无法获取当前用户信息";
        }

        List<UserFriend> friends = userFriendMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserFriend>()
                        .eq(UserFriend::getUserId, userId)
        );

        if (friends == null || friends.isEmpty()) {
            return "您还没有好友，请先添加好友";
        }

        List<String> friendList = new ArrayList<>();
        for (UserFriend friend : friends) {
            User user = userMapper.selectById(friend.getFriendId());
            if (user != null) {
                String remark = friend.getRemark();
                String displayName = (remark != null && !remark.isEmpty()) ? remark : user.getRealName();
                friendList.add(displayName + "(ID:" + user.getId() + ")");
            }
        }

        return "您的好友列表（共" + friends.size() + "人）：\n" + String.join("\n", friendList);
    }

    public String searchUserByName(Map<String, Object> arguments) {
        String userName = (String) arguments.get("userName");

        if (userName == null || userName.trim().isEmpty()) {
            return "错误：请提供要搜索的用户名（userName）";
        }

        List<User> users = userMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .like(User::getRealName, userName.trim())
                        .last("LIMIT 10")
        );

        if (users == null || users.isEmpty()) {
            return "未找到名为 '" + userName + "' 的用户";
        }

        List<String> results = new ArrayList<>();
        for (User user : users) {
            results.add("用户名：" + user.getRealName() + "，ID：" + user.getId());
        }

        return "搜索到以下用户：\n" + String.join("\n", results);
    }

    private UserFriend findFriendByName(Integer userId, String name) {
        List<UserFriend> friends = userFriendMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserFriend>()
                        .eq(UserFriend::getUserId, userId)
        );

        for (UserFriend friend : friends) {
            User user = userMapper.selectById(friend.getFriendId());
            if (user != null) {
                boolean nameMatch = name.equals(user.getRealName()) || name.equals(friend.getRemark());
                if (name.equals(user.getUsername())) {
                    return friend;
                }
                if (nameMatch) {
                    return friend;
                }
            }
        }

        return null;
    }

    private String buildChatId(Integer userId1, Integer userId2) {
        int minId = Math.min(userId1, userId2);
        int maxId = Math.max(userId1, userId2);
        return "user-" + minId + "-" + maxId;
    }
}