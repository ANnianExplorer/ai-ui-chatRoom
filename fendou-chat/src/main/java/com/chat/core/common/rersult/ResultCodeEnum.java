package com.chat.core.common.rersult;

import lombok.Getter;

/**
 * 统一返回结果状态枚举类
 *
 * @author y
 * @since 2026-01-05
 */
@Getter
public enum ResultCodeEnum {

    SUCCESS(200, "操作成功！"),
    FAIL(201, "操作失败！"),

    USER_NOT_LOGIN(401, "未登录"),
    SERVER_ERROR(500, "服务器异常！"),
    USERNAME_PASSWORD_ERROR(1000, "用户名或密码错误！"),
    OLD_PASSWORD_ERROR(1003, "旧密码错误！"),
    FILE_UPLOAD_ERROR(1004, "文件上传失败"),
    FILE_NOT_FIND(1005, "文件不存在"),
    USERNAME_EXISTS(1006, "用户已存在"),
    USER_NOT_EXISTS(1007, "目标用户不存在"),
    FRIEND_REQUEST_ERROR(1008, "已向对方发送过请求，请勿重复请求！"),
    GROUP_NOT_EXISTS(1009, "群组不存在"),
    CAPTCHA_NOT_EXISTS(1010, "验证码无效"),
    CAPTCHA_ERROR(1011, "验证码错误"),
    FILE_TOO_LARGE(1012, "文件太大"),
    CANNOT_SEND_REQUEST_TO_SELF(1013, "无法向自己发送请求");


    // 状态码
    private final Integer code;
    // 消息
    private final String message;

    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
