package com.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chat.domain.dto.group.GroupAddDTO;
import com.chat.domain.pojo.Group;
import com.chat.domain.pojo.GroupMember;

import java.util.List;

/**
 * @author y
 * @since 2026-01-07
 */
public interface GroupService extends IService<Group> {

    List<Group> getGroupsByUserId(Integer userId);

    Boolean create(GroupAddDTO groupAddDTO);

    Boolean quit(Integer groupId, Integer userId);

    Boolean addMember(String chatId, Integer friendId);

    List<Integer> getMembers(Integer groupId);

    List<Integer> getMembersAdmin(Integer groupId);

    Boolean updateRemark(GroupMember groupMember);

    boolean verifyGroup(Integer groupId, Integer userId);
}
