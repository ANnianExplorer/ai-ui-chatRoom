package com.chat.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.chat.core.common.constant.CacheConstants;
import com.chat.core.common.constant.CommonConstants;
import com.chat.core.common.constant.SecurityConstants;
import com.chat.core.common.excpetion.CustomException;
import com.chat.core.common.rersult.ResultCodeEnum;
import com.chat.core.common.util.AvatarGeneratorUtils;
import com.chat.core.common.util.JwtUtils;
import com.chat.domain.converter.UserConverter;
import com.chat.domain.dto.auth.LoginDTO;
import com.chat.domain.dto.user.UserRegisterDTO;
import com.chat.domain.pojo.File;
import com.chat.domain.pojo.User;
import com.chat.service.AuthService;
import com.chat.service.FileService;
import com.chat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author y
 * @since 2026-01-07
 */

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserService userService;
    @Autowired
    private FileService fileService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${netty.host}")
    private String nettyHost;
    @Value("${netty.port}")
    private Integer nettyPort;

    @Override
    public Map<String, String> login(LoginDTO loginDTO) {

        String username = loginDTO.getUsername();
        
        // // 管理员登录不需要验证码验证（默认用户名为admin）
        // if (!"admin".equals(username)) {
        //     // 验证验证码
        //     validateCode(loginDTO);
        // }

        // 根据用户名称查询用户
        User user = userService.getUserByUsername(username);

        // 判断用户是否存在
        if (ObjectUtil.isEmpty(user)) {
            throw new CustomException(ResultCodeEnum.USERNAME_PASSWORD_ERROR);
        }

        // 判断密码是否正确
        String salt = user.getSalt();
        if (!DigestUtil.md5Hex(loginDTO.getPassword() + salt).equals(user.getPassword())) {
            throw new CustomException(ResultCodeEnum.USERNAME_PASSWORD_ERROR);
        }
        
        // 检查用户状态
        if (user.getStatus() == CommonConstants.DISABLED_STATUS) {
            throw new CustomException("账号已被禁用，无法登录");
        }

        // 生成 token
        Map<String, String> claimMap = new HashMap<>();
        claimMap.put("id", user.getId().toString());
        claimMap.put("username", user.getUsername());
        String token = JwtUtils.createToken(claimMap);

        // 将用户信息保存到 redis 中
        redisTemplate.opsForValue().set(CacheConstants.USER_KEY + token, user, SecurityConstants.EXPIRE_TIME, TimeUnit.MILLISECONDS);
        // 一小时内清除缓存
        redisTemplate.expire(CacheConstants.USER_KEY + token, SecurityConstants.EXPIRE_TIME, TimeUnit.MILLISECONDS);

        // 封装结果
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("ws", nettyHost + ":" + nettyPort);

        return result;
    }

    @Override
    public Boolean register(UserRegisterDTO userRegisterDTO) {
        String username = userRegisterDTO.getUsername();

        if (userService.getUserByUsername(username) != null) {
            throw new CustomException(username + ResultCodeEnum.USERNAME_EXISTS.getMessage());
        }

        User user = UserConverter.INSTANCE.toEntity(userRegisterDTO);
        String randomSalt = RandomUtil.randomString(SecurityConstants.SALT_SIZE);
        user.setSalt(randomSalt);
        user.setPassword(DigestUtil.md5Hex(user.getPassword() + randomSalt));

        // 生成默认头像
        byte[] bytes = AvatarGeneratorUtils.generateAvatarAsBytes(username, CommonConstants.DEFAULT_IMAGE_SIZE);
        File file = fileService.upload(bytes, username + "." + CommonConstants.DEFAULT_IMAGE_SUFFIX, CommonConstants.DEFAULT_IMAGE_TYPE);
        user.setAvatar(file.getFilePath());

        return userService.save(user);
    }

    @Override
    public String captcha(String randomStr) {
        // 定义图形验证码的长和宽
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(100, 40, SecurityConstants.CAPTCHA_SIZE, 100);

        // 验证码值
        String code = lineCaptcha.getCode();

        // 将验证码缓存到 redis 中，1 分钟后失效
        redisTemplate.opsForValue().set(CacheConstants.CAPTCHA_KEY + randomStr, code, SecurityConstants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);

        // 验证码以base64的格式返回到客户端
        return lineCaptcha.getImageBase64Data();
    }

    @Override
    public User getInfo(String token) {
        User user = (User) redisTemplate.opsForValue().get(CacheConstants.USER_KEY + token.substring(7));

        if (user == null) {
            throw new CustomException(ResultCodeEnum.USER_NOT_LOGIN);
        }

        return user;
    }

    @Override
    public Boolean logout(String token) {
        return redisTemplate.delete(CacheConstants.USER_KEY + token.substring(7));
    }


    private void validateCode(LoginDTO loginDTO) {
        String randomStr = loginDTO.getRandomStr();
        String code = loginDTO.getCode();

        // 获取缓存中的验证码
        String cacheCode = (String) redisTemplate.opsForValue().get(CacheConstants.CAPTCHA_KEY + randomStr);

        // 判断验证码是否存在
        if (ObjectUtil.isEmpty(cacheCode)) {
            throw new CustomException(ResultCodeEnum.CAPTCHA_NOT_EXISTS);
        }

        // 验证验证码
        if (!code.equalsIgnoreCase(cacheCode)) {
            throw new CustomException(ResultCodeEnum.CAPTCHA_ERROR);
        }

        // 删除验证码
        redisTemplate.delete(CacheConstants.CAPTCHA_KEY + randomStr);
    }

    public static void main(String[] args) {
        String s = DigestUtil.md5Hex(123456 + "test2");
        System.out.println(s);
    }
}
