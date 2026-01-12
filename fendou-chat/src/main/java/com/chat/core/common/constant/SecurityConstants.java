package com.chat.core.common.constant;

/**
 * 安全相关常量
 *
 * @author y
 * @since 2026-01-05
 */
public interface SecurityConstants {

    /**
     * 密码盐长度
     */
    int SALT_SIZE = 12;

    /**
     * 验证码长度
     */
    int CAPTCHA_SIZE = 5;

    /**
     * 验证码过期时间
     */
    long CAPTCHA_EXPIRATION = 1;

    /**
     * token 过期时间
     */
    Long EXPIRE_TIME = 8 * 60 * 60 * 1000L; // 8 小时

    /**
     * token 加密密钥
     */
    String SECRET = "my_secret";

    /**
     * AI 用户信息
     */
    Integer AI_USER_ID = 0;
    String AI_NAME = "AI聊天";
    String AI_AVATAR = "https://img2.baidu.com/it/u=2473380155,1807338550&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500";
    String AI_REMARK = "我的智能聊天搭子";
    String AI_DESCRIPTION = "我是你的智能聊天搭子，有什么问题可以问我哦！我尽可能为你提供帮助呀！";
}
