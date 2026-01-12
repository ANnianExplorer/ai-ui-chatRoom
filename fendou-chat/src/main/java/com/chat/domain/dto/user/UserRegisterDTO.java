package com.chat.domain.dto.user;

import lombok.Data;

/**
 * 用户注册数据传输对象
 * @author y
 * @since 2026-01-07
 */

@Data
public class UserRegisterDTO {

    private String username;
    private String password;
    private String realName;
    private String email;
}
