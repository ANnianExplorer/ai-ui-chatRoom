package com.chat.domain.dto.auth;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 登录数据传输对象
 *
 * @author y
 * @since 2026-01-07
 */

@Data
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "验证码不能为空")
    private String code;

    @NotBlank(message = "验证码 key 不能为空")
    private String randomStr;
}
