package com.chat.core.common.tools;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chat.core.common.handler.ChatWebSocketHandler;
import com.chat.domain.dto.ai.QueryFriends;
import com.chat.domain.pojo.Group;
import com.chat.domain.pojo.Message;
import com.chat.domain.pojo.User;
import com.chat.domain.pojo.UserFriend;
import com.chat.domain.vo.QueryFriendsVO;
import com.chat.domain.vo.QueryGroupsVO;
import com.chat.mapper.UserFriendMapper;
import com.chat.service.GroupService;
import com.chat.service.MessageService;
import com.chat.service.UserService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * AI 消息工具（Function Calling）
 * 使用 Spring AI @Tool 注解声明能力，Spring AI 自动生成工具 Schema 并注册到 ChatClient。
 * userId 通过 ThreadLocal 注入，不暴露给 AI 模型。调用方必须在 Function Calling 执行前
 * 调用 setCurrentUserId(userId) 并在 finally 块中调用 clearCurrentUserId()。
 *
 * @author y
 * @since 2026/4/2
 **/
@Slf4j
@Component
public class MessageTool {

    /**
     * 每次 AI 代理调用前，由 AIServiceImpl 在同一线程中设置当前用户 ID。
     * 使用 CompletableFuture.runAsync 确保工具执行与 ThreadLocal 在同一线程中。
     */
    private static final ThreadLocal<Integer> CURRENT_USER_ID = new ThreadLocal<>();

    public static void setCurrentUserId(Integer userId) {
        CURRENT_USER_ID.set(userId);
    }

    public static void clearCurrentUserId() {
        CURRENT_USER_ID.remove();
    }

    // ──────────────────────── 依赖注入 ────────────────────────

    @Resource
    private UserService userService;

    @Resource
    private MessageService messageService;

    @Resource
    private UserFriendMapper userFriendMapper;

    @Resource
    private GroupService groupService;

    // ──────────────────────── @Tool 方法 ────────────────────────

    /**
     * 查询当前用户的好友列表
     */
    @Tool(description = "查询当前登录用户的好友列表，返回好友ID和昵称")
    public List<QueryFriendsVO> queryFriendList(
            @ToolParam(description = "查询条件（可传空对象 {} 查全部好友）", required = false)
            QueryFriends queryFriends) {

        Integer userId = CURRENT_USER_ID.get();
        if (userId == null) return List.of();

        QueryWrapper<UserFriend> wrapper = new QueryWrapper<UserFriend>()
                .eq("user_id", userId);

        if (queryFriends != null) {
            if (queryFriends.getStatus() != null) {
                wrapper.eq("status", queryFriends.getStatus());
            }
            if (queryFriends.getIsDeleted() != null) {
                wrapper.eq("is_deleted", queryFriends.getIsDeleted());
            }
            if (queryFriends.getSorts() != null) {
                for (QueryFriends.Sort sort : queryFriends.getSorts()) {
                    if (sort.getField() != null) {
                        wrapper.orderBy(true, Boolean.TRUE.equals(sort.getAsc()), sort.getField());
                    }
                }
            }
        }

        List<Integer> friendIds = userFriendMapper.selectList(wrapper)
                .stream().map(UserFriend::getFriendId).toList();

        return friendIds.stream().map(fid -> {
            User friend = userService.getUserById(fid);
            QueryFriendsVO vo = new QueryFriendsVO();
            vo.setFriendId(fid);
            vo.setFriendName(friend != null ? friend.getRealName() : "未知用户");
            return vo;
        }).toList();
    }

    /**
     * 查询当前用户所加入的群聊列表
     */
    @Tool(description = "查询当前登录用户所加入的所有群聊，返回群聊ID和名称")
    public List<QueryGroupsVO> queryGroupList() {
        Integer userId = CURRENT_USER_ID.get();
        if (userId == null) return List.of();

        return groupService.getGroupsByUserId(userId).stream().map(g -> {
            QueryGroupsVO vo = new QueryGroupsVO();
            vo.setGroupId(g.getId());
            vo.setName(g.getName());
            return vo;
        }).toList();
    }

    /**
     * 给好友发送私聊消息（保存到 DB + WebSocket 实时推送）
     */
    @Tool(description = "给指定好友发送一条私聊文本消息，发送成功后接收方会实时收到消息通知")
    public String sendFriendMessage(
            @ToolParam(description = "好友的昵称或备注名称") String friendName,
            @ToolParam(description = "要发送的消息内容") String content) {

        Integer userId = CURRENT_USER_ID.get();
        if (userId == null) return "错误：未获取到当前用户信息，请重新登录";
        if (friendName == null || friendName.isBlank()) return "错误：请提供好友名称";
        if (content == null || content.isBlank()) return "错误：消息内容不能为空";

        UserFriend friendRelation = findFriendByName(userId, friendName);
        if (friendRelation == null) {
            return "错误：未找到名为「" + friendName + "」的好友，请检查名称是否正确";
        }

        Integer targetUserId = friendRelation.getFriendId();
        User sender = userService.getUserById(userId);
        User target = userService.getUserById(targetUserId);
        if (target == null) return "错误：目标好友信息不存在";

        String chatId = buildPrivateChatId(userId, targetUserId);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Message msg = new Message();
        msg.setChatId(chatId);
        msg.setContent(content);
        msg.setContentType("text");
        msg.setSendId(userId);
        msg.setIsRead(0);
        msg.setStatus(1);
        msg.setIsDeleted(0);
        boolean saved = messageService.save(msg);
        if (!saved) return "错误：消息保存失败";

        JSONObject pushPayload = new JSONObject();
        pushPayload.put("chatId", chatId);
        pushPayload.put("sendId", userId);
        pushPayload.put("content", content);
        pushPayload.put("contentType", "text");
        pushPayload.put("createTime", now);
        pushPayload.put("realName", sender != null ? sender.getRealName() : "未知");
        pushPayload.put("chatAvatar", sender != null ? sender.getAvatar() : "");
        pushToUser(targetUserId, pushPayload.toJSONString());

        String targetName = (friendRelation.getRemark() != null && !friendRelation.getRemark().isBlank())
                ? friendRelation.getRemark() : target.getRealName();
        log.info("AI代理发送消息给好友 {}(id={}), chatId={}", targetName, targetUserId, chatId);
        return "SUCCESS: 消息已成功发送给「" + targetName + "」";
    }

    /**
     * 给群聊发送消息（保存到 DB + WebSocket 实时推送给所有群成员）
     */
    @Tool(description = "给指定群聊发送一条文本消息，群里所有在线成员都会实时收到消息通知")
    public String sendGroupMessage(
            @ToolParam(description = "群聊名称") String groupName,
            @ToolParam(description = "要发送的消息内容") String content) {

        Integer userId = CURRENT_USER_ID.get();
        if (userId == null) return "错误：未获取到当前用户信息，请重新登录";
        if (groupName == null || groupName.isBlank()) return "错误：请提供群聊名称";
        if (content == null || content.isBlank()) return "错误：消息内容不能为空";

        List<Group> userGroups = groupService.getGroupsByUserId(userId);
        Group targetGroup = userGroups.stream()
                .filter(g -> groupName.equals(g.getName()))
                .findFirst().orElse(null);

        if (targetGroup == null) {
            return "错误：未找到名为「" + groupName + "」的群聊，或你未加入该群。" +
                    "你加入的群聊有：" + userGroups.stream().map(Group::getName).toList();
        }

        User sender = userService.getUserById(userId);
        String chatId = "group-" + targetGroup.getId();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Message msg = new Message();
        msg.setChatId(chatId);
        msg.setContent(content);
        msg.setContentType("text");
        msg.setSendId(userId);
        msg.setIsRead(1);
        msg.setStatus(1);
        msg.setIsDeleted(0);
        boolean saved = messageService.save(msg);
        if (!saved) return "错误：消息保存失败";

        JSONObject pushPayload = new JSONObject();
        pushPayload.put("chatId", chatId);
        pushPayload.put("sendId", userId);
        pushPayload.put("content", content);
        pushPayload.put("contentType", "text");
        pushPayload.put("createTime", now);
        pushPayload.put("realName", sender != null ? sender.getRealName() : "未知");
        pushPayload.put("chatAvatar", sender != null ? sender.getAvatar() : "");
        pushToGroup(targetGroup.getId(), pushPayload.toJSONString());

        log.info("AI代理发送消息到群「{}」(groupId={}), userId={}", groupName, targetGroup.getId(), userId);
        return "SUCCESS: 消息已成功发送到群「" + groupName + "」";
    }

    // ──────────────────────── 私有工具方法 ────────────────────────

    private UserFriend findFriendByName(Integer userId, String name) {
        List<UserFriend> friends = userFriendMapper.selectList(
                new LambdaQueryWrapper<UserFriend>().eq(UserFriend::getUserId, userId));
        for (UserFriend uf : friends) {
            User user = userService.getUserById(uf.getFriendId());
            if (user == null) continue;
            if (name.equals(user.getRealName())
                    || name.equals(user.getUsername())
                    || name.equals(uf.getRemark())) {
                return uf;
            }
        }
        return null;
    }

    private String buildPrivateChatId(Integer id1, Integer id2) {
        int min = Math.min(id1, id2), max = Math.max(id1, id2);
        return "user-" + min + "-" + max;
    }

    private void pushToUser(Integer targetUserId, String jsonStr) {
        ChannelId channelId = ChatWebSocketHandler.userMap.get(targetUserId);
        if (channelId != null) {
            Channel channel = ChatWebSocketHandler.channelGroup.find(channelId);
            if (channel != null && channel.isActive()) {
                channel.writeAndFlush(new TextWebSocketFrame(jsonStr));
                log.debug("WebSocket 推送给用户 {}", targetUserId);
            }
        }
    }

    private void pushToGroup(Integer groupId, String jsonStr) {
        ChannelGroup group = ChatWebSocketHandler.groupMap.get(groupId);
        if (group != null) {
            group.writeAndFlush(new TextWebSocketFrame(jsonStr));
            log.debug("WebSocket 推送给群 {}", groupId);
        }
    }
}
