package com.chat.controller.admin;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.core.common.handler.ChatWebSocketHandler;
import com.chat.core.common.rersult.Result;
import com.chat.domain.pojo.SystemAnnouncement;
import com.chat.mapper.SystemAnnouncementMapper;
import com.chat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 管理后台操作控制器（封禁、禁言、公告）
 *
 * @author y
 * @since 2026-04-01
 */
@RestController
@RequestMapping("/admin/actions")
public class AdminActionsController {

    @Autowired
    private UserService userService;

    @Autowired
    private SystemAnnouncementMapper announcementMapper;

    /**
     * 封禁/解封用户
     */
    @PostMapping("/ban/{userId}")
    public Result<?> banUser(@PathVariable Integer userId, @RequestBody Map<String, Object> body) {
        boolean banned = Boolean.TRUE.equals(body.get("banned"));
        boolean success = userService.banUser(userId, banned);
        if (success && banned) {
            // 强制下线：通知WebSocket断开该用户连接
            io.netty.channel.ChannelId channelId = ChatWebSocketHandler.userMap.get(userId);
            if (channelId != null) {
                io.netty.channel.Channel channel = ChatWebSocketHandler.channelGroup.find(channelId);
                if (channel != null) {
                    JSONObject notice = new JSONObject();
                    notice.put("type", "FORCE_LOGOUT");
                    notice.put("content", "您的账号已被管理员封禁");
                    channel.writeAndFlush(new io.netty.handler.codec.http.websocketx.TextWebSocketFrame(notice.toJSONString()));
                    channel.close();
                }
            }
        }
        return Result.ok(success);
    }

    /**
     * 群禁言/解除禁言
     */
    @PostMapping("/mute")
    public Result<?> muteUser(@RequestBody Map<String, Object> body) {
        Integer userId = Integer.valueOf(body.get("userId").toString());
        Integer groupId = Integer.valueOf(body.get("groupId").toString());
        boolean muted = Boolean.TRUE.equals(body.get("muted"));
        Integer duration = body.get("duration") != null ? Integer.valueOf(body.get("duration").toString()) : 60;

        boolean success = userService.setUserMute(userId, groupId, muted, duration);

        // 通知被禁言用户
        if (success) {
            io.netty.channel.ChannelId channelId = ChatWebSocketHandler.userMap.get(userId);
            if (channelId != null) {
                io.netty.channel.Channel channel = ChatWebSocketHandler.channelGroup.find(channelId);
                if (channel != null) {
                    JSONObject notice = new JSONObject();
                    notice.put("type", "MUTE_NOTICE");
                    notice.put("muted", muted);
                    notice.put("groupId", groupId);
                    notice.put("duration", duration);
                    notice.put("content", muted ? "您已被管理员禁言 " + duration + " 分钟" : "您已被解除禁言");
                    channel.writeAndFlush(new io.netty.handler.codec.http.websocketx.TextWebSocketFrame(notice.toJSONString()));
                }
            }
        }
        return Result.ok(success);
    }

    /**
     * 向所有在线用户广播系统公告（同时保存推送历史）
     */
    @PostMapping("/announce")
    public Result<?> sendAnnouncement(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        String title = body.get("title");
        if (content == null || content.trim().isEmpty()) {
            return Result.fail("公告内容不能为空").message("公告内容不能为空");
        }
        
        // 广播公告
        JSONObject notice = new JSONObject();
        notice.put("title", title != null ? title : "系统公告");
        notice.put("content", content);
        ChatWebSocketHandler.broadcastSystemNotice(notice);
        
        // 保存推送历史到数据库
        SystemAnnouncement announcement = new SystemAnnouncement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setOnlineCount(ChatWebSocketHandler.userMap.size());
        announcement.setStatus(1);
        announcement.setIsDeleted(0);
        announcement.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        announcementMapper.insert(announcement);
        
        return Result.ok("公告发送成功");
    }

    /**
     * 查询系统公告推送历史（分页）
     */
    @GetMapping("/announcements")
    public Result<?> getAnnouncements(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<SystemAnnouncement> result = announcementMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<SystemAnnouncement>()
                        .eq(SystemAnnouncement::getIsDeleted, 0)
                        .orderByDesc(SystemAnnouncement::getCreateTime)
        );
        return Result.ok(result);
    }
}
