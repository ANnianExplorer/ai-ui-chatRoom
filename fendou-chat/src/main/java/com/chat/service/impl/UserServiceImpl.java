package com.chat.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chat.core.common.constant.CacheConstants;
import com.chat.domain.converter.UserConverter;
import com.chat.domain.dto.user.UserPasswordDTO;
import com.chat.domain.dto.user.UserProfileDTO;
import com.chat.domain.dto.user.UserQueryDTO;
import com.chat.domain.pojo.User;
import com.chat.core.common.excpetion.CustomException;
import com.chat.mapper.UserMapper;
import com.chat.service.FriendService;
import com.chat.service.UserService;
import com.chat.core.common.rersult.ResultCodeEnum;
import com.chat.core.common.util.SecurityHolderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author y
 * @since 2026-01-07
 */

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    @Lazy
    private FriendService friendService;

    //@Cacheable(value = "friends", keyGenerator = "keyGenerator")
    @Override
    public Page<User> getFriends(Integer pIndex, Integer pSize, UserQueryDTO userQueryDTO) {
        Page<User> page = new Page<>(pIndex, pSize);
        List<User> users = baseMapper.selectUserFriends(page, userQueryDTO);
        // 去掉禁用的用户
        users.removeIf(item -> item.getStatus() == 0);
        page.setRecords(users);
        return page;
    }


    @Override
    public List<User> getUsersByGroupId(Integer groupId) {
        return baseMapper.selectUsersByGroupId(groupId);
    }

    @Override
    public User getUserByUsername(String username) {
        return baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    public Integer verifyFriend(Integer userId, Integer friendId) {
        Boolean b1 = friendService.verifyFriend(userId, friendId);
        Boolean b2 = friendService.verifyFriend(friendId, userId);

        if(!b1 && b2){
            return 1;
        }

        if(b1 && !b2){
            return 2;
        }

        return 0;
    }


    @Override
    public Boolean updateProfile(UserProfileDTO userProfileDTO) {
        User user = UserConverter.INSTANCE.toEntity(userProfileDTO);
        Boolean b = super.updateById(user);

        // 更新缓存
        redisTemplate.opsForValue().set(CacheConstants.USER_KEY + SecurityHolderUtils.getToken(), baseMapper.selectById(user.getId()));

        return b;
    }

    @Override
    public Boolean updatePassword(UserPasswordDTO userPasswordDTO) {
        User user = (User)redisTemplate.opsForValue().get(CacheConstants.USER_KEY + SecurityHolderUtils.getToken());
        if(user == null){
            throw new CustomException("修改密码失败");
        }

        String salt = user.getSalt();

        // 判断旧密码是否正确
        if (!user.getPassword().equals(DigestUtil.md5Hex(userPasswordDTO.getOldPwd() + salt))) {
            throw new CustomException(ResultCodeEnum.OLD_PASSWORD_ERROR);
        }


        // 设置新密码
        User u = new User();
        u.setId(user.getId());
        u.setPassword(DigestUtil.md5Hex(userPasswordDTO.getNewPwd() + salt));

        return super.updateById(u);
    }

    @Override
    public boolean isUserMuted(Integer userId, Integer groupId) {
        String key = "mute:" + groupId + ":" + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public boolean setUserMute(Integer userId, Integer groupId, boolean muted, Integer durationMinutes) {
        String key = "mute:" + groupId + ":" + userId;
        if (muted) {
            if (durationMinutes != null && durationMinutes > 0) {
                redisTemplate.opsForValue().set(key, "1", durationMinutes, TimeUnit.MINUTES);
            } else {
                redisTemplate.opsForValue().set(key, "1", 24 * 60, TimeUnit.MINUTES);
            }
        } else {
            redisTemplate.delete(key);
        }
        return true;
    }

    @Override
    public boolean banUser(Integer userId, boolean banned) {
        User user = new User();
        user.setId(userId);
        user.setStatus(banned ? 0 : 1);
        return super.updateById(user);
    }

    @Override
    public User getUserById(Integer friendId) {
        return baseMapper.selectById(friendId);
    }
}
