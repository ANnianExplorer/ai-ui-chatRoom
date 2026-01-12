package com.chat.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.domain.dto.group.GroupCallUpdateRequest;
import com.chat.mapper.GroupMemberMapper;
import com.chat.domain.pojo.Group;
import com.chat.domain.pojo.GroupMember;
import com.chat.domain.pojo.User;
import com.chat.service.GroupService;
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
 * 管理员群聊控制器
 *
 * @author y
 * @since 2026-01-09
 */
@RequestMapping("/admin/groups")
@RestController
public class AdminGroupController {

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private GroupMemberMapper groupMemberMapper;

    /**
     * 获取所有群聊列表
     *
     * @param page     当前页码
     * @param pageSize 每页大小
     * @param keyword  搜索关键词
     * @param status   群聊状态
     * @return 群聊列表
     */
    @GetMapping
    public Result<?> getGroups(@RequestParam(name = "page", defaultValue = "1") Integer page,
                              @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize,
                              @RequestParam(name = "keyword", required = false) String keyword,
                              @RequestParam(name = "status", required = false) String status) {
        // 构建查询条件
        LambdaQueryWrapper<Group> queryWrapper = new LambdaQueryWrapper<>();
        
        // 处理搜索关键词
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(Group::getName, keyword)
                       .or().like(Group::getId, keyword);
        }
        
        // 处理状态筛选
        if (status != null && !status.isEmpty()) {
            try {
                Integer statusInt = Integer.parseInt(status);
                queryWrapper.eq(Group::getStatus, statusInt);
            } catch (NumberFormatException e) {
                // 忽略格式错误，不进行状态筛选
            }
        }
        
        // 执行分页查询
        Page<Group> groupPage = groupService.page(new Page<>(page, pageSize), queryWrapper);
        
        // 转换为前端期望的格式
        List<Map<String, Object>> formattedGroups = new ArrayList<>();
        for (Group group : groupPage.getRecords()) {
            Map<String, Object> formatted = new HashMap<>();
            
            // 基础信息
            formatted.put("id", group.getId());
            formatted.put("name", group.getName());
            formatted.put("avatar", group.getAvatar());
            formatted.put("status", group.getStatus());
            formatted.put("createdAt", group.getCreateTime());
            formatted.put("groupCall", group.getGroupCall());
            
            // 非数据库字段，需要单独处理
            formatted.put("isOfficial", false); // 假设默认非官方群，根据实际业务调整
            formatted.put("lastMessageTime", null); // 假设暂无最后消息时间，根据实际业务调整
            
            // 创建者信息（需要根据createBy字段查询用户信息）
            Integer createBy = group.getCreateBy();
            User creator = userService.getById(createBy);
            if (creator != null) {
                formatted.put("creator", creator.getUsername());
                formatted.put("creatorAvatar", creator.getAvatar());
            } else {
                formatted.put("creator", "未知用户");
                formatted.put("creatorAvatar", "");
            }
            
            // 成员数量（需要查询群成员数量）
            List<Integer> memberIds = groupService.getMembersAdmin(group.getId());
            formatted.put("memberCount", memberIds != null ? memberIds.size() : 0);
            
            formattedGroups.add(formatted);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", formattedGroups);
        result.put("total", groupPage.getTotal());
        
        return Result.ok(result);
    }

    /**
     * 获取单个群聊信息
     *
     * @param id 群聊ID
     * @return 群聊信息
     */
    @GetMapping("/{id}")
    public Result<?> getGroupById(@PathVariable("id") Integer id) {
        Group group = groupService.getById(id);
        
        // 转换为前端期望的格式
        Map<String, Object> formatted = new HashMap<>();
        
        // 基础信息
        formatted.put("id", group.getId());
        formatted.put("name", group.getName());
        formatted.put("avatar", group.getAvatar());
        formatted.put("description", group.getDescription());
        formatted.put("status", group.getStatus());
        formatted.put("createdAt", group.getCreateTime());
        formatted.put("groupCall", group.getGroupCall());


        // 非数据库字段，需要单独处理
        formatted.put("isOfficial", false); // 假设默认非官方群，根据实际业务调整
        formatted.put("lastMessageTime", null); // 假设暂无最后消息时间，根据实际业务调整
        
        // 创建者信息（需要根据createBy字段查询用户信息）
        Integer createBy = group.getCreateBy();
        User creator = userService.getById(createBy);
        if (creator != null) {
            formatted.put("creator", creator.getUsername());
            formatted.put("creatorAvatar", creator.getAvatar());
        } else {
            formatted.put("creator", "未知用户");
            formatted.put("creatorAvatar", "");
        }
        
        // 成员数量（需要查询群成员数量）
        List<Integer> memberIds = groupService.getMembersAdmin(id);
        formatted.put("memberCount", memberIds != null ? memberIds.size() : 0);
        
        return Result.ok(formatted);
    }

    /**
     * 获取群聊成员
     *
     * @param id 群聊ID
     * @return 群聊成员列表
     */
    @GetMapping("/{id}/members")
    public Result<?> getGroupMembers(@PathVariable("id") Integer id) {
        // 查询group_member表获取成员信息，包括createTime
        LambdaQueryWrapper<GroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GroupMember::getGroupId, id);
        List<GroupMember> groupMembers = groupMemberMapper.selectList(queryWrapper);
        
        // 获取成员详情信息
        List<Map<String, Object>> members = new ArrayList<>();
        if (groupMembers != null && !groupMembers.isEmpty()) {
            // 收集所有成员ID
            List<Integer> memberIds = groupMembers.stream()
                    .map(GroupMember::getUserId)
                    .collect(Collectors.toList());
            
            // 查询用户信息
            List<User> userList = userService.listByIds(memberIds);
            Map<Integer, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, u -> u));
            
            // 构建成员列表
            for (GroupMember groupMember : groupMembers) {
                Integer userId = groupMember.getUserId();
                User user = userMap.get(userId);
                if (user != null) {
                    Map<String, Object> member = new HashMap<>();
                    member.put("id", user.getId());
                    member.put("username", user.getUsername());
                    member.put("avatar", user.getAvatar());
                    member.put("role", "member"); // 假设默认成员角色，根据实际业务调整
                    member.put("joinTime", groupMember.getCreateTime()); // 使用group_member表中的create_time作为加入时间
                    members.add(member);
                }
            }
        }
        
        return Result.ok(members);
    }


    /**
     * 更新群聊状态
     *
     * @param id     群聊ID
     * @param status 状态（active/disabled）
     * @return 是否更新成功
     */
    @PutMapping("/{id}/status")
    public Result<?> updateGroupStatus(@PathVariable("id") Integer id,
                                      @RequestBody StatusUpdateRequest status) {
        Group group = new Group();
        group.setId(id);
        group.setStatus(status.getStatus());
        boolean updated = groupService.updateById(group);
        return updated ? Result.ok() : Result.fail();
    }

    /**
     * 删除群聊
     *
     * @param id 群聊ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteGroup(@PathVariable("id") Integer id) {
        boolean deleted = groupService.removeById(id);
        return deleted ? Result.ok() : Result.fail();
    }

    /**
     * 状态更新请求DTO
     */
    public static class StatusUpdateRequest {
        private Integer status;

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }
}
