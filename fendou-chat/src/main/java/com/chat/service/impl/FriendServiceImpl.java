package com.chat.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.core.common.constant.CommonConstants;
import com.chat.core.common.enums.FriendRequestStatusEnum;
import com.chat.core.common.excpetion.CustomException;
import com.chat.core.common.rersult.ResultCodeEnum;
import com.chat.core.common.util.SecurityHolderUtils;
import com.chat.domain.dto.friend.FriendAddDTO;
import com.chat.domain.dto.friend.FriendQueryDTO;
import com.chat.domain.pojo.FriendRequest;
import com.chat.domain.pojo.Message;
import com.chat.domain.pojo.User;
import com.chat.domain.pojo.UserFriend;
import com.chat.domain.vo.FriendRequestVO;
import com.chat.mapper.FriendRequestMapper;
import com.chat.mapper.MessageMapper;
import com.chat.mapper.UserFriendMapper;
import com.chat.service.FriendService;
import com.chat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * @author y
 * @since 2026-01-07
 */

@Service
public class FriendServiceImpl implements FriendService {

    @Autowired
    private FriendRequestMapper friendRequestMapper;
    @Autowired
    private UserFriendMapper userFriendMapper;

    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private UserService userService;

    @Override
    public Boolean addFriend(FriendAddDTO friendAddDTO) {
        Integer userId = SecurityHolderUtils.getUserId();
        String friendInfo = friendAddDTO.getFriendInfo();
        String verifyMsg = friendAddDTO.getVerifyMsg();

        boolean b = friendInfo.matches("-?\\d+?");
        Integer receiverId;

        if (b) {
            receiverId = Integer.parseInt(friendInfo);
        } else {
            User user = userService.getUserByUsername(friendInfo);
            if (user == null) {
                throw new CustomException(ResultCodeEnum.USER_NOT_EXISTS);
            }
            receiverId = user.getId();
        }

        if(userId.equals(receiverId)){
            throw new CustomException(ResultCodeEnum.CANNOT_SEND_REQUEST_TO_SELF);
        }

        LambdaQueryWrapper<FriendRequest> lqw = new LambdaQueryWrapper<>();
        lqw.eq(FriendRequest::getSenderId, userId);
        lqw.eq(FriendRequest::getReceiverId, receiverId);
        lqw.eq(FriendRequest::getStatus, FriendRequestStatusEnum.PENDING.getCode());

        FriendRequest fq = friendRequestMapper.selectOne(lqw);
        if (fq != null) {
            throw new CustomException(ResultCodeEnum.FRIEND_REQUEST_ERROR);
        }

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSenderId(userId);
        friendRequest.setReceiverId(receiverId);
        friendRequest.setVerifyMsg(verifyMsg);

        return friendRequestMapper.insert(friendRequest) > 0;
    }

    //@CacheEvict(value = "friends", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean respondFriend(Integer id, Integer status) {
        // 修改好友请求状态
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setId(id);
        friendRequest.setStatus(status);
        friendRequestMapper.updateById(friendRequest);

        // 如果同意
        if(Objects.equals(status, FriendRequestStatusEnum.AGREED.getCode())){
            FriendRequest friend = friendRequestMapper.selectById(id);

            Integer userId = friend.getSenderId();
            Integer friendId = friend.getReceiverId();

            // 保存好友关系
            saveFriend(userId, friendId);
            saveFriend(friendId, userId);
        }

        return true;
    }


    private void saveFriend(Integer userId, Integer friendId){
        LambdaQueryWrapper<UserFriend> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserFriend::getUserId, userId);
        lqw.eq(UserFriend::getFriendId, friendId);
        UserFriend userFriend = userFriendMapper.selectOne(lqw);
        if (userFriend == null) {
            // 保存好友关系
            UserFriend uf = new UserFriend();
            uf.setUserId(userId);
            uf.setFriendId(friendId);
            userFriendMapper.insert(uf);
        }
    }


    @Override
    public Page<FriendRequestVO> getFriendRequests(Integer pIndex, Integer pSize, FriendQueryDTO friendQueryDTO) {
        Page<FriendRequestVO> page = new Page<>(pIndex, pSize);
        friendQueryDTO.setUserId(SecurityHolderUtils.getUserId());
        page.setRecords(friendRequestMapper.selectFriendRequests(page, friendQueryDTO));
        return page;
    }

    //@CacheEvict(value = "friends", allEntries = true)
    @Override
    public Boolean deleteFriend(Integer userId, Integer friendId) {
        LambdaQueryWrapper<UserFriend> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserFriend::getUserId, userId);
        lqw.eq(UserFriend::getFriendId, friendId);

        String chatId1 = "user-" + userId + "-" + friendId;
        String chatId2 = "user-" + friendId + "-" + userId;
        LambdaQueryWrapper<Message> q = new LambdaQueryWrapper<>();
        // 要么等于chatId1 要么等于chatId2
        q.and(wrapper -> wrapper.eq(Message::getChatId, chatId1).or().eq(Message::getChatId, chatId2));

        if (userFriendMapper.delete(lqw) > 0) {
            // 直接删除聊天记录
            messageMapper.delete(q);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public Boolean verifyFriend(Integer userId, Integer friendId) {
        LambdaQueryWrapper<UserFriend> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserFriend::getUserId, userId);
        lqw.eq(UserFriend::getFriendId, friendId);

        return userFriendMapper.selectOne(lqw) != null;
    }

    @Override
    public Boolean updateFriend(UserFriend userFriend) {
        LambdaQueryWrapper<UserFriend> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserFriend::getFriendId, userFriend.getFriendId());
        lqw.eq(UserFriend::getUserId, SecurityHolderUtils.getUserId());

        return userFriendMapper.update(userFriend, lqw) > 0;
    }

    @Override
    public Long getFriendRequestCount() {
        LambdaQueryWrapper<FriendRequest> lqw = new LambdaQueryWrapper<>();
        lqw.eq(FriendRequest::getReceiverId, SecurityHolderUtils.getUserId());
        lqw.eq(FriendRequest::getStatus, FriendRequestStatusEnum.PENDING.getCode());
        return friendRequestMapper.selectCount(lqw);
    }

    @Override
    public boolean verifyIfFriend(Integer userId, Integer friendId) {
        LambdaQueryWrapper<UserFriend> lqw = new LambdaQueryWrapper<>();
        if (friendId == 0 || friendId.equals(userId)) {
            return false;
        }
        lqw.eq(UserFriend::getUserId, userId)
                .eq(UserFriend::getFriendId, friendId);
        UserFriend userFriend = userFriendMapper.selectOne(lqw);
        return userFriend != null;
    }
}
