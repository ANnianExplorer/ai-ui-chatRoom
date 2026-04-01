package com.chat.core.common.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.domain.dto.user.UserQueryDTO;
import com.chat.core.common.enums.MessageTypeEnum;
import com.chat.core.common.enums.UserStatusEnum;
import com.chat.domain.pojo.Group;
import com.chat.domain.pojo.Message;
import com.chat.domain.pojo.User;
import com.chat.service.GroupService;
import com.chat.service.MessageService;
import com.chat.service.UserService;
import com.chat.core.common.util.CommonUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.ReferenceCounted;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author y
 * @since 2026-01-07
 */

@Slf4j
public class ChatWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final UserService userService;
    private final GroupService groupService;
    private final MessageService messageService;

    public ChatWebSocketHandler(UserService userService, GroupService groupService, MessageService messageService) {
        this.userService = userService;
        this.groupService = groupService;
        this.messageService = messageService;
    }

    /**
     * 存储已经登录用户的channel对象
     */
    public static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 存储用户id和用户的channelId绑定
     */
    public static ConcurrentHashMap<Integer, ChannelId> userMap = new ConcurrentHashMap<>();
    /**
     * 用于存储群聊房间号和群聊成员的channel信息
     */
    public static ConcurrentHashMap<Integer, ChannelGroup> groupMap = new ConcurrentHashMap<>();


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("{} 用户上线了", ctx.channel());

        // 添加到 channelGroup 中
        channelGroup.add(ctx.channel());

        ctx.channel().id();
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("{} 用户离线了", ctx.channel());

        // 查找并移除用户ID
        Integer offlineUserId = null;
        for (Integer userId : userMap.keySet()) {
            if (userMap.get(userId).equals(ctx.channel().id())) {
                offlineUserId = userId;
                break;
            }
        }

        if (offlineUserId != null) {
            userMap.remove(offlineUserId);
            broadcastUserStatus(offlineUserId, UserStatusEnum.OFFLINE.getCode());
        }

        // 移除用户ID和 channelId 的绑定
        channelGroup.remove(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Integer userId;

        // 首次连接是FullHttpRequest，把用户id和对应的channel对象存储起来
        if (msg instanceof FullHttpRequest request) {

            // 获取 url 中的参数
            String uri = request.uri();
            userId = CommonUtils.getUrlParams(uri);

            if (userId != null) {
                // 存储用户ID和 channelId 的绑定
                userMap.put(userId, ctx.channel().id());

                // 广播自己上线
                broadcastUserStatus(userId, UserStatusEnum.ONLINE.getCode());

                // 初始好友状态
                initUserFriendStatus(userId, ctx);

                // 第1次登录,获取用户加入的群聊加入到 群聊组 中
                initUserToGroup(userId, ctx);

                // 如果url包含参数，需要处理
                if (uri.contains("?")) {
                    String newUri = uri.substring(0, uri.indexOf("?"));
                    request.setUri(newUri);
                }
            }

        } else if (msg instanceof TextWebSocketFrame frame) {
            log.info("收到客户端消息：{}", frame.text());
            
            // 解析为JSONObject，先判断消息类型
            JSONObject jsonMessage = JSON.parseObject(frame.text());
            String messageType = jsonMessage.getString("type");
            
            // 处理请求类型消息
            if (MessageTypeEnum.isType(messageType, MessageTypeEnum.REQUEST_USER_STATUS)) {
                log.info("收到用户状态请求");
                Integer currentUserId = findUserIdByChannel(ctx.channel());
                if (currentUserId != null) {
                    initUserFriendStatus(currentUserId, ctx);
                }
                return;
            } else if (MessageTypeEnum.isType(messageType, MessageTypeEnum.REQUEST_GROUP_ONLINE_COUNT)) {
                log.info("收到群聊在线人数请求");
                Integer currentUserId = findUserIdByChannel(ctx.channel());
                if (currentUserId != null) {
                    initUserToGroup(currentUserId, ctx);
                }
                return;
            }

            // 处理正在输入消息（不保存，直接转发）
            if (MessageTypeEnum.isType(messageType, MessageTypeEnum.TYPING.name())) {
                handleTypingMessage(jsonMessage);
                return;
            }

            // 处理消息撤回
            if (MessageTypeEnum.isType(messageType, MessageTypeEnum.RECALL.name())) {
                handleRecallMessage(jsonMessage);
                return;
            }

            // 处理系统公告广播
            if (MessageTypeEnum.isType(messageType, MessageTypeEnum.SYSTEM_NOTICE.name())) {
                broadcastSystemNotice(jsonMessage);
                return;
            }
            
            // 正常的TEXT消息类型
            Message message = JSON.parseObject(frame.text(), Message.class);

            // 处理心跳消息
            if (MessageTypeEnum.isType(message.getContentType(), MessageTypeEnum.HEARTBEAT)) {
                log.info("收到用户心跳消息");

                Map<String, Object> result = new HashMap<>();
                result.put("type", MessageTypeEnum.HEARTBEAT_ACK);
                result.put("timestamp", System.currentTimeMillis());
                Channel ct = channelGroup.find(ctx.channel().id());
                if (ct != null) {
                    ct.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(result)));
                }

                return;
            }

            String chatId = message.getChatId();
            // 获取接收者id
            int receiverId = Integer.parseInt(chatId.substring(chatId.lastIndexOf("-") + 1));

            // 处理群聊消息
            if (chatId.contains("group")) {
                log.info("群聊成员: {} 群聊消息: {}", groupMap, message);
                
                // 检查群聊状态
                Group group = groupService.getById(receiverId);
                if (group == null || group.getStatus() == 0) {
                    sendToUser(message.getSendId(), MessageTypeEnum.ERROR, "该群聊已被禁用，无法发送消息！");
                    return;
                }

                // 检查用户禁言状态
                if (userService.isUserMuted(message.getSendId(), receiverId)) {
                    sendToUser(message.getSendId(), MessageTypeEnum.ERROR, "您已被群主禁言，无法发送消息！");
                    return;
                }

                // 推送群聊信息
                ChannelGroup groupChannels = groupMap.get(receiverId);
                if (groupChannels != null) {
                    groupChannels.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(message)));
                }
            }

            // 处理好友消息
            else {
                String[] parts = chatId.split("-");
                Integer user1 = Integer.parseInt(parts[1]);
                int user2 = Integer.parseInt(parts[2]);
                receiverId = Objects.equals(message.getSendId(), user1) ? user2 : user1;

                // 验证好友关系
                Integer status = userService.verifyFriend(message.getSendId(), receiverId);
                if (status == 1) {
                    sendToUser(message.getSendId(), MessageTypeEnum.ERROR, "无法向已删除的用户发送消息！");
                    return;
                } else if (status == 2) {
                    sendToUser(message.getSendId(), MessageTypeEnum.ERROR, "消息发送失败，您不是对方好友！");
                    return;
                }

                // 处理私聊的消息
                ChannelId channelId = userMap.get(receiverId);

                if (channelId != null) {
                    Channel ct = channelGroup.find(channelId);
                    if (ct != null) {
                        log.info("好友消息: {}", message);
                        ct.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(message)));
                    }
                }
            }

            // 保存消息（定时消息除外）
            if (message.getScheduledTime() == null || message.getScheduledTime().isEmpty()) {
                messageService.save(message);
            }
        }
        super.channelRead(ctx, msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        // 已在 channelRead() 中处理，所以这里不需要处理
    }


    /**
     * 广播上线离线消息
     *
     * @param userId 用户ID
     * @param status 状态
     */
    private void broadcastUserStatus(Integer userId, Integer status) {
        // 构建消息
        JSONObject message = new JSONObject();
        message.put("type", MessageTypeEnum.USER_STATUS);
        message.put("userId", userId);
        message.put("status", status);
        message.put("timestamp", System.currentTimeMillis());
        String jsonStr = message.toJSONString();

        // 遍历所有在线用户
        channelGroup.forEach(channel -> {
            if (channel.isActive()) {
                channel.writeAndFlush(new TextWebSocketFrame(jsonStr));
            }
        });

        log.info("广播用户状态-->{}", jsonStr);
    }

    /**
     * 发送消息给指定用户
     *
     * @param userId  用户ID
     * @param type    消息类型
     * @param content 消息内容
     */
    private void sendToUser(Integer userId, MessageTypeEnum type, String content) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", type);
        result.put("content", content);
        result.put("timestamp", System.currentTimeMillis());

        ChannelId channelId = userMap.get(userId);
        if (channelId != null) {
            Channel ct = channelGroup.find(channelId);
            if (ct != null) {
                ct.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(result)));
            }
        }

        log.info("发送消息给用户-->{}", result);
    }


    /**
     * 广播群聊在线人数
     *
     * @param groupId 群聊ID
     */
    private void broadcastGroupOnlineCount(Integer groupId) {
        JSONObject message = new JSONObject();
        message.put("type", MessageTypeEnum.GROUP_ONLINE_COUNT);
        message.put("chatId", "group-" + groupId);
        message.put("onlineCount", groupMap.get(groupId).size());
        message.put("timestamp", System.currentTimeMillis());
        log.info("广播群聊在线人数-->{}", message);
        ChannelGroup group = groupMap.get(groupId);
        if (group != null) {
            group.writeAndFlush(new TextWebSocketFrame(message.toJSONString()));
        }
    }

    /**
     * 初始化用户好友状态
     *
     * @param userId 用户Id
     * @param ctx    ChannelHandlerContext
     */
    private void initUserFriendStatus(Integer userId, ChannelHandlerContext ctx) {
        // 查询用户好友
        UserQueryDTO userQueryDTO = new UserQueryDTO();
        userQueryDTO.setUserId(userId);
        Page<User> friends = userService.getFriends(0, -1, userQueryDTO);

        // 构建消息
        JSONObject statusMsg = new JSONObject();
        statusMsg.put("type", MessageTypeEnum.INIT_USER_STATUS);
        statusMsg.put("timestamp", System.currentTimeMillis());
        JSONArray list = new JSONArray();
        for (User friend : friends.getRecords()) {
            Integer friendId = friend.getId();
            JSONObject obj = new JSONObject();
            obj.put("userId", friendId);
            // 如果userMap包含好友ID，则在线（0），否则离线（1）
            // 在线状态通过WebSocket连接状态判断，与用户的status字段无关
            obj.put("status", userMap.containsKey(friendId) ? 0 : 1);
            list.add(obj);
        }
        statusMsg.put("friends", list);

        // 发送给当前登录用户
        ctx.channel().eventLoop().schedule(() -> {
            ctx.channel().writeAndFlush(new TextWebSocketFrame(statusMsg.toJSONString()));
        }, 500, TimeUnit.MILLISECONDS); // 延迟 500ms 再发送

        log.info("初始化好友状态消息: {}", statusMsg);
    }


    /**
     * 根据Channel查找用户ID
     *
     * @param channel Channel
     * @return 用户ID
     */
    private Integer findUserIdByChannel(Channel channel) {
        for (Map.Entry<Integer, ChannelId> entry : userMap.entrySet()) {
            if (entry.getValue().equals(channel.id())) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * 将用户初始化到加入的群聊组中
     *
     * @param userId 用户Id
     * @param ctx    ChannelHandlerContext
     */
    private void initUserToGroup(Integer userId, ChannelHandlerContext ctx) {
        // 查询用户加入的群聊
        List<Group> groups = groupService.getGroupsByUserId(userId);
        groups.forEach(group -> {
            Integer groupId = group.getId();
            // 获取群聊的 channelGroup
            ChannelGroup channelGroup = groupMap.get(groupId);
            if (channelGroup == null) {
                channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
                groupMap.put(groupId, channelGroup);
            }
            channelGroup.add(ctx.channel());

            // 广播群聊在线人数
            ctx.channel().eventLoop().schedule(() -> {
                broadcastGroupOnlineCount(groupId);
            }, 500, TimeUnit.MILLISECONDS); // 延迟 500ms 再发送
        });

    }

    /**
     * 处理正在输入消息（直接转发，不保存）
     */
    private void handleTypingMessage(JSONObject jsonMessage) {
        String chatId = jsonMessage.getString("chatId");
        Integer sendId = jsonMessage.getInteger("sendId");
        if (chatId == null || sendId == null) return;

        if (chatId.contains("group")) {
            int groupId = Integer.parseInt(chatId.substring(chatId.lastIndexOf("-") + 1));
            ChannelGroup groupChannels = groupMap.get(groupId);
            if (groupChannels != null) {
                String jsonStr = jsonMessage.toJSONString();
                groupChannels.forEach(channel -> {
                    ChannelId cid = userMap.get(sendId);
                    if (cid == null || !cid.equals(channel.id())) {
                        channel.writeAndFlush(new TextWebSocketFrame(jsonStr));
                    }
                });
            }
        } else {
            String[] parts = chatId.split("-");
            if (parts.length >= 3) {
                Integer user1 = Integer.parseInt(parts[1]);
                Integer user2 = Integer.parseInt(parts[2]);
                Integer receiverId = Objects.equals(sendId, user1) ? user2 : user1;
                ChannelId channelId = userMap.get(receiverId);
                if (channelId != null) {
                    Channel ct = channelGroup.find(channelId);
                    if (ct != null) {
                        ct.writeAndFlush(new TextWebSocketFrame(jsonMessage.toJSONString()));
                    }
                }
            }
        }
    }

    /**
     * 处理消息撤回
     */
    private void handleRecallMessage(JSONObject jsonMessage) {
        String chatId = jsonMessage.getString("chatId");
        Integer msgId = jsonMessage.getInteger("msgId");
        Integer sendId = jsonMessage.getInteger("sendId");
        if (chatId == null || msgId == null || sendId == null) return;

        // 更新数据库消息状态
        Message recallMsg = new Message();
        recallMsg.setId(msgId);
        recallMsg.setIsRecalled(1);
        messageService.updateById(recallMsg);

        String jsonStr = jsonMessage.toJSONString();
        if (chatId.contains("group")) {
            int groupId = Integer.parseInt(chatId.substring(chatId.lastIndexOf("-") + 1));
            ChannelGroup groupChannels = groupMap.get(groupId);
            if (groupChannels != null) {
                groupChannels.writeAndFlush(new TextWebSocketFrame(jsonStr));
            }
        } else {
            String[] parts = chatId.split("-");
            if (parts.length >= 3) {
                Integer user1 = Integer.parseInt(parts[1]);
                Integer user2 = Integer.parseInt(parts[2]);
                Integer receiverId = Objects.equals(sendId, user1) ? user2 : user1;
                ChannelId channelId = userMap.get(receiverId);
                if (channelId != null) {
                    Channel ct = channelGroup.find(channelId);
                    if (ct != null) ct.writeAndFlush(new TextWebSocketFrame(jsonStr));
                }
                // 同时发给自己（其他设备）
                ChannelId selfChannelId = userMap.get(sendId);
                if (selfChannelId != null) {
                    Channel selfCt = channelGroup.find(selfChannelId);
                    if (selfCt != null) selfCt.writeAndFlush(new TextWebSocketFrame(jsonStr));
                }
            }
        }
        log.info("消息撤回处理完成, msgId: {}", msgId);
    }

    /**
     * 广播系统公告给所有在线用户
     */
    public static void broadcastSystemNotice(JSONObject noticeData) {
        noticeData.put("type", MessageTypeEnum.SYSTEM_NOTICE.name());
        noticeData.put("timestamp", System.currentTimeMillis());
        String jsonStr = noticeData.toJSONString();
        channelGroup.forEach(channel -> {
            if (channel.isActive()) {
                channel.writeAndFlush(new TextWebSocketFrame(jsonStr));
            }
        });
    }
}
