package com.chat.controller;

import com.chat.domain.dto.group.GroupAddDTO;
import com.chat.domain.dto.group.GroupCallUpdateRequest;
import com.chat.domain.dto.group.GroupRemoveDTO;
import com.chat.domain.pojo.Group;
import com.chat.domain.pojo.GroupMember;
import com.chat.service.GroupService;
import com.chat.core.common.rersult.Result;
import com.chat.core.common.util.SecurityHolderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 群聊控制器
 *
 * @author y
 * @since 2026-01-07
 */

@RestController
@RequestMapping("group")
public class GroupController {


    @Autowired
    private GroupService groupService;

    @PostMapping("create")
    public Result<?> create(@RequestBody GroupAddDTO groupAddDTO) {
        return groupService.create(groupAddDTO) ? Result.ok() : Result.fail();
    }

    @PostMapping("addMember")
    public Result<?> addMember(@RequestParam("chatId") String chatId, @RequestParam("friendId") Integer friendId) {
        return groupService.addMember(chatId, friendId) ? Result.ok() : Result.fail();
    }

    @PostMapping("join")
    public Result<?> joinMember(@RequestParam("chatId") String chatId,
                                @RequestParam("friendId") Integer friendId) {
        return groupService.addMember(chatId, friendId) ? Result.ok() : Result.fail();
    }

    @GetMapping("/quit/{id}")
    public Result<?> quit(@PathVariable("id") Integer id) {
        return groupService.quit(id, SecurityHolderUtils.getUserId()) ? Result.ok() : Result.fail();
    }
    
    @PostMapping("/remove")
    public Result<?> removeMember(@RequestBody GroupRemoveDTO groupRemoveDTO) {
        return groupService.quit(groupRemoveDTO.getGroupId(), groupRemoveDTO.getUserId()) ? Result.ok() : Result.fail();
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable("id") Integer id) {
        return groupService.removeById(id) ? Result.ok() : Result.fail();
    }

    @GetMapping("/members/{id}")
    public Result<?> getMembers(@PathVariable("id") String chatId) {
        Integer groupId = Integer.parseInt(chatId.substring(chatId.lastIndexOf("-") + 1));
        return Result.ok(groupService.getMembers(groupId));
    }


    @GetMapping("/groups/{userId}")
    public Result<?> getFriends(@PathVariable("userId") Integer userId) {
        return Result.ok(groupService.getGroupsByUserId(userId));
    }

    @GetMapping("/get/{id}")
    public Result<?> getById(@PathVariable("id") Integer id) {
        return Result.ok(groupService.getById(id));
    }

    @PutMapping("/update-remark")
    public Result<?> updateRemark(@RequestBody GroupMember groupMember) {
        return groupService.updateRemark(groupMember) ? Result.ok() : Result.fail();
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody Group group) {
        return groupService.updateById(group) ? Result.ok() : Result.fail();
    }


    /**
     * 更新群聊公告
     * @param request
     * @return
     */
    @PutMapping("/updateCall")
    public Result<?> updateGroupCall(@RequestBody GroupCallUpdateRequest request) {
        if (request == null) {
            throw new RuntimeException("参数错误");
        }
        Integer groupId = request.getId();
        Group oldGroup = groupService.getById(groupId);
        if (oldGroup == null) {
            throw new RuntimeException("群聊不存在");
        }
        if (!oldGroup.getCreateBy().equals(SecurityHolderUtils.getUserId())) {
            throw new RuntimeException("没有群主权限");
        }
        oldGroup.setGroupCall(request.getGroupCall());
        boolean b = groupService.saveOrUpdate(oldGroup);
        return b ? Result.ok() : Result.fail();
    }

    @GetMapping("/getCall/{id}")
    public Result<?> getGroupCall(@PathVariable("id") Integer id) {
        Group oldGroup = groupService.getById(id);
        if (oldGroup == null) {
            throw new RuntimeException("群聊不存在");
        }
        return Result.ok(oldGroup.getGroupCall());
    }

    /**
     * 判断用户是否已经加群了
     */
    @GetMapping("/verifyG/{groupId}")
    public Result<?> verifyGroup(@PathVariable("groupId") Integer groupId) {
        return Result.ok(groupService.verifyGroup(groupId, SecurityHolderUtils.getUserId()));
    }
}
