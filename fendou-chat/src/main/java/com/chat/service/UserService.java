package com.chat.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chat.domain.dto.user.UserPasswordDTO;
import com.chat.domain.dto.user.UserProfileDTO;
import com.chat.domain.dto.user.UserQueryDTO;
import com.chat.domain.pojo.User;

import java.util.List;

/**
 * @author y
 * @since 2026-01-07
 */
public interface UserService extends IService<User> {

    /**
     * 更新用户信息
     *
     * @param userProfileDTO 用户更新数据传输对象
     * @return 是否更新成功
     */
    Boolean updateProfile(UserProfileDTO userProfileDTO);

    /**
     * 更新用户密码
     *
     * @param userPasswordDTO 用户更新密码数据传输对象
     * @return 是否更新成功
     */
    Boolean updatePassword(UserPasswordDTO userPasswordDTO);


    Page<User> getFriends(Integer pIndex, Integer pSize, UserQueryDTO userQueryDTO);

    List<User> getUsersByGroupId(Integer groupId);


    User getUserByUsername(String username);


    Integer verifyFriend(Integer userId, Integer friendId);
}
