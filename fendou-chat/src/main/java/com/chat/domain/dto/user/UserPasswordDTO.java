package com.chat.domain.dto.user;

import lombok.Data;

/**
 * 修改密码数据传输对象
 * @author y
 * @since 2026-01-07
 */

@Data
public class UserPasswordDTO {

    // 旧密码
    private String oldPwd;

    // 新密码
    private String newPwd;

}
