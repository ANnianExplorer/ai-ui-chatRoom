package com.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.domain.dto.friend.FriendQueryDTO;
import com.chat.domain.pojo.FriendRequest;
import com.chat.domain.vo.FriendRequestVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author y
 * @since 2026-01-07
 */
public interface FriendRequestMapper extends BaseMapper<FriendRequest> {

    List<FriendRequestVO> selectFriendRequests(Page<FriendRequestVO> page, @Param("friendRequest") FriendQueryDTO friendQueryDTO);
}
