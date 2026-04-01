package com.chat.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chat.core.common.handler.ChatWebSocketHandler;
import com.chat.core.common.rersult.Result;
import com.chat.domain.pojo.Message;
import com.chat.domain.pojo.User;
import com.chat.service.MessageService;
import com.chat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 管理后台统计控制器
 *
 * @author y
 * @since 2026-04-01
 */
@RestController
@RequestMapping("/admin/statistics")
public class AdminStatisticsController {

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    /**
     * 数据概览（总用户数、在线人数、今日消息数、总消息数）
     */
    @GetMapping("/overview")
    public Result<?> getOverview() {
        Map<String, Object> data = new HashMap<>();

        long totalUsers = userService.count(new QueryWrapper<User>().eq("is_deleted", 0));
        data.put("totalUsers", totalUsers);
        data.put("onlineUsers", ChatWebSocketHandler.userMap.size());

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        long todayMessages = messageService.count(
                new QueryWrapper<Message>()
                        .eq("is_deleted", 0)
                        .likeRight("create_time", today)
        );
        data.put("todayMessages", todayMessages);
        data.put("totalMessages", messageService.count(new QueryWrapper<Message>().eq("is_deleted", 0)));

        return Result.ok(data);
    }

    /**
     * 近30天注册趋势
     */
    @GetMapping("/register-trend")
    public Result<?> getRegisterTrend() {
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter displayFmt = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 29; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(fmt);
            long count = userService.count(
                    new QueryWrapper<User>()
                            .eq("is_deleted", 0)
                            .likeRight("create_time", dateStr)
            );
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.format(displayFmt));
            item.put("count", count);
            result.add(item);
        }
        return Result.ok(result);
    }

    /**
     * 近30天消息量趋势
     */
    @GetMapping("/message-trend")
    public Result<?> getMessageTrend() {
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter displayFmt = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 29; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(fmt);
            long count = messageService.count(
                    new QueryWrapper<Message>()
                            .eq("is_deleted", 0)
                            .likeRight("create_time", dateStr)
            );
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.format(displayFmt));
            item.put("count", count);
            result.add(item);
        }
        return Result.ok(result);
    }

    /**
     * 24小时活跃时段热力图（0-23时各时段消息量）
     */
    @GetMapping("/hourly-heatmap")
    public Result<?> getHourlyHeatmap() {
        List<Map<String, Object>> result = new ArrayList<>();
        // 获取近7天每小时消息数
        List<Message> recentMessages = messageService.list(
                new QueryWrapper<Message>()
                        .eq("is_deleted", 0)
                        .ge("create_time", LocalDate.now().minusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
        );

        int[] hourCounts = new int[24];
        for (Message msg : recentMessages) {
            if (msg.getCreateTime() != null && msg.getCreateTime().length() >= 13) {
                try {
                    int hour = Integer.parseInt(msg.getCreateTime().substring(11, 13));
                    if (hour >= 0 && hour < 24) {
                        hourCounts[hour]++;
                    }
                } catch (Exception ignored) {}
            }
        }

        for (int h = 0; h < 24; h++) {
            Map<String, Object> item = new HashMap<>();
            item.put("hour", h + ":00");
            item.put("count", hourCounts[h]);
            result.add(item);
        }
        return Result.ok(result);
    }

    /**
     * 用户活跃度分析（近7天活跃用户数、留存率等）
     */
    @GetMapping("/user-activity")
    public Result<?> getUserActivity() {
        Map<String, Object> data = new HashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 近7天每日活跃用户数（发过消息的唯一用户数）
        List<Map<String, Object>> dailyActive = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(fmt);
            List<Message> dayMessages = messageService.list(
                    new QueryWrapper<Message>()
                            .eq("is_deleted", 0)
                            .likeRight("create_time", dateStr)
            );
            Set<Integer> uniqueUsers = new HashSet<>();
            for (Message m : dayMessages) {
                if (m.getSendId() != null) uniqueUsers.add(m.getSendId());
            }
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.format(DateTimeFormatter.ofPattern("MM-dd")));
            item.put("activeUsers", uniqueUsers.size());
            dailyActive.add(item);
        }
        data.put("dailyActive", dailyActive);

        // 用户消息频率分布（按发送消息数分组）
        List<Message> allMessages = messageService.list(
                new QueryWrapper<Message>().eq("is_deleted", 0)
        );
        Map<Integer, Long> userMsgCount = new HashMap<>();
        for (Message m : allMessages) {
            if (m.getSendId() != null) {
                userMsgCount.merge(m.getSendId(), 1L, Long::sum);
            }
        }
        long veryActive = userMsgCount.values().stream().filter(c -> c > 100).count();
        long active = userMsgCount.values().stream().filter(c -> c > 20 && c <= 100).count();
        long normal = userMsgCount.values().stream().filter(c -> c > 5 && c <= 20).count();
        long inactive = userMsgCount.values().stream().filter(c -> c <= 5).count();
        List<Map<String, Object>> distribution = List.of(
                Map.of("name", "非常活跃(>100条)", "value", veryActive),
                Map.of("name", "活跃(21-100条)", "value", active),
                Map.of("name", "普通(6-20条)", "value", normal),
                Map.of("name", "不活跃(≤5条)", "value", inactive)
        );
        data.put("activityDistribution", distribution);

        return Result.ok(data);
    }
}
