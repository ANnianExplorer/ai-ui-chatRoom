package com.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.domain.dto.user.UserQueryDTO;
import com.chat.domain.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author y
 * @since 2026-01-07
 */

@Mapper
public interface UserMapper extends BaseMapper<User> {

    List<User> selectUserFriends(Page<User> page, @Param("user") UserQueryDTO userQueryDTO);

    List<User> selectUsersByGroupId(Integer groupId);
}
