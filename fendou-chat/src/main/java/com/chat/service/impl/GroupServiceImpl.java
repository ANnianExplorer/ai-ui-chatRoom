package com.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chat.core.common.constant.CommonConstants;
import com.chat.domain.dto.group.GroupAddDTO;
import com.chat.domain.pojo.Group;
import com.chat.domain.pojo.GroupMember;
import com.chat.domain.pojo.User;
import com.chat.core.common.excpetion.CustomException;
import com.chat.mapper.GroupMapper;
import com.chat.mapper.GroupMemberMapper;
import com.chat.service.GroupService;
import com.chat.service.UserService;
import com.chat.core.common.rersult.ResultCodeEnum;
import com.chat.core.common.util.SecurityHolderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @author y
 * @since 2026-01-07
 */

@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements GroupService {

    @Autowired
    private GroupMemberMapper groupMemberMapper;

    @Autowired
    private UserService userService;

    @Override
    public Group getById(Serializable id) {
        Group group = super.getById(id);

        if (group == null) {
            throw new CustomException(ResultCodeEnum.GROUP_NOT_EXISTS);
        }

        List<User> list = userService.getUsersByGroupId(group.getId());
        group.setMembers(list);

        return group;
    }

    //@Cacheable(value = "groups", keyGenerator = "keyGenerator")
    @Override
    public List<Group> getGroupsByUserId(Integer userId) {
        List<Group> groups = baseMapper.selectGroupsByUserId(userId);
        // 去掉被禁用的群聊
        // 假设有状态常量
        groups.removeIf(item -> Objects.equals(item.getStatus(), CommonConstants.DISABLED_STATUS));
        return groups;
    }

    @CacheEvict(value = "groups", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean create(GroupAddDTO groupAddDTO) {
        Integer userId = SecurityHolderUtils.getUserId();

        Group group = new Group();
        group.setName(groupAddDTO.getName());
        group.setDescription(groupAddDTO.getDescription());
        group.setRemark(groupAddDTO.getRemark());
        group.setCreateBy(userId);

        Boolean b = super.save(group);

        if (b) {
            GroupMember member = new GroupMember();
            member.setGroupId(group.getId());
            member.setUserId(userId);

            groupMemberMapper.insert(member);
        }

        return b;
    }

    //@CacheEvict(value = "groups", allEntries = true)
    @Override
    public boolean removeById(Group entity) {
        return super.removeById(entity);
    }

    //@CacheEvict(value = "groups", allEntries = true)
    @Override
    public Boolean quit(Integer groupId, Integer userId) {
        // 检查群聊状态
        Group group = getById(groupId);
        if (group == null) {
            throw new CustomException(ResultCodeEnum.GROUP_NOT_EXISTS);
        }
        if (Objects.equals(group.getStatus(), CommonConstants.DISABLED_STATUS)) {
            throw new CustomException("该群聊已被禁用，无法退出群聊");
        }

        LambdaQueryWrapper<GroupMember> lqw = new LambdaQueryWrapper<>();
        lqw.eq(GroupMember::getGroupId, groupId);
        lqw.eq(GroupMember::getUserId, userId);

        // 直接物理删除，不使用逻辑删除
        return groupMemberMapper.delete(lqw) > 0;
    }

    @Override
    public Boolean addMember(String chatId, Integer friendId) {
        Integer groupId = Integer.parseInt(chatId.substring(chatId.lastIndexOf("-") + 1));
        
        // 检查群聊状态
        Group group = getById(groupId);
        if (group == null) {
            throw new CustomException(ResultCodeEnum.GROUP_NOT_EXISTS);
        }
        if (group.getStatus() == CommonConstants.DISABLED_STATUS) {
            throw new CustomException("该群聊已被禁用，无法添加成员");
        }
        
        GroupMember groupMember = new GroupMember();
        groupMember.setGroupId(groupId);
        groupMember.setUserId(friendId);

        return groupMemberMapper.insert(groupMember) > 0;
    }

    @Override
    public List<Integer> getMembers(Integer groupId) {
        // 检查群聊状态
        Group group = getById(groupId);
        if (group == null) {
            throw new CustomException(ResultCodeEnum.GROUP_NOT_EXISTS);
        }
        if (Objects.equals(group.getStatus(), CommonConstants.DISABLED_STATUS)) {
            throw new CustomException("该群聊已被禁用，无法获取成员列表");
        }
        return groupMemberMapper.selectObjs(new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .select(GroupMember::getUserId)
        );
    }

    @Override
    public List<Integer> getMembersAdmin(Integer groupId) {
        // 检查群聊状态
        Group group = getById(groupId);
        if (group == null) {
            throw new CustomException(ResultCodeEnum.GROUP_NOT_EXISTS);
        }

        return groupMemberMapper.selectObjs(new LambdaQueryWrapper<GroupMember>()
                .eq(GroupMember::getGroupId, groupId)
                .select(GroupMember::getUserId)
        );
    }
    @Override
    public Boolean updateRemark(GroupMember groupMember) {
        // 检查群聊状态
        Group group = getById(groupMember.getGroupId());
        if (group == null) {
            throw new CustomException(ResultCodeEnum.GROUP_NOT_EXISTS);
        }
        if (group.getStatus() == CommonConstants.DISABLED_STATUS) {
            throw new CustomException("该群聊已被禁用，无法更新备注");
        }
        
        LambdaQueryWrapper<GroupMember> lqw = new LambdaQueryWrapper<>();
        lqw.eq(GroupMember::getGroupId, groupMember.getGroupId());
        lqw.eq(GroupMember::getUserId, SecurityHolderUtils.getUserId());

        return groupMemberMapper.update(groupMember, lqw) > 0;
    }

    @Override
    public boolean verifyGroup(Integer groupId, Integer userId) {
        LambdaQueryWrapper<GroupMember> lqw = new LambdaQueryWrapper<>();
        lqw.eq(GroupMember::getGroupId, groupId);
        lqw.eq(GroupMember::getUserId, userId);
        GroupMember groupMember = groupMemberMapper.selectOne(lqw);
        if (groupMember == null) {
            return false;
        }
        // 判断是否被删除
        return Objects.equals(groupMember.getIsDeleted(), CommonConstants.NOT_DELETED_STATUS);
    }

}
