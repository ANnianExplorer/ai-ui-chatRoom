package com.chat.domain.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改用户数据传输对象
 * @author y
 * @since 2026-01-07
 */

@Data
public class UserProfileDTO {

    private Integer id;

    @NotBlank(message = "真实姓名不能为空!")
    private String realName;

    private String avatar;

    private String birthday;

    private Integer gender;

    private String phone;

    private String address;

    private String description;
}
