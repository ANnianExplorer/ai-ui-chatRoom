package com.chat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.domain.dto.friend.FriendAddDTO;
import com.chat.domain.dto.friend.FriendQueryDTO;
import com.chat.domain.pojo.UserFriend;
import com.chat.domain.vo.FriendRequestVO;

/**
 * @author y
 * @since 2026-01-07
 */
public interface FriendService {

    /**
     * 添加好友
     *
     * @return 添加结果
     */
    Boolean addFriend(FriendAddDTO friendAddDTO);

    /**
     * 响应好友请求
     *
     * @param id     好友请求ID
     * @param status 响应状态
     * @return 添加结果
     */
    Boolean respondFriend(Integer id, Integer status);


    /**
     * 获取好友请求
     *
     * @return 好友请求列表
     */
    Page<FriendRequestVO> getFriendRequests(Integer pIndex, Integer pSize, FriendQueryDTO friendQueryDTO);


    /**
     * 删除好友
     *
     * @param userId   用户ID
     * @param friendId 好友ID
     * @return 删除结果
     */
    Boolean deleteFriend(Integer userId, Integer friendId);

    /**
     * 验证好友关系
     *
     * @param userId   用户ID
     * @param friendId 好友ID
     * @return 验证结果
     */
    Boolean verifyFriend(Integer userId, Integer friendId);

    /**
     * 修改好友信息
     *
     * @param userFriend 好友信息
     * @return 修改结果
     */
    Boolean updateFriend(UserFriend userFriend);


    /**
     * 获取好友请求数量
     *
     * @return 好友请求数量
     */
    Long getFriendRequestCount();

    boolean verifyIfFriend(Integer userId, Integer friendId);
}
