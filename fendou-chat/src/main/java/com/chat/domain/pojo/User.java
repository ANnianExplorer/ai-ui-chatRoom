package com.chat.domain.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 *
 * @author y
 * @since 2026-01-07
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class User extends BasePOJO {

    // 用户名称
    private String username;

    // 用户密码
    private String password;

    // 盐值
    private String salt;

    // 真实姓名
    private String realName;

    // 用户性别
    private Integer gender;

    // 出生日期
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String birthday;

    // 用户手机号
    private String phone;

    // 用户头像
    private String avatar;

    // 用户邮箱
    private String email;

    // 用户地址
    private String address;

    // 描述
    private String description;


    @TableField(exist = false)
    private String chatId;

    @TableField(exist = false)
    private Integer unreadCount;

    @TableField(exist = false)
    private String remark;
}

