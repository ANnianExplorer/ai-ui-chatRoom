package com.chat.core.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户状态枚举
 *
 * @author y
 * @since 2026-01-07
 */

@Getter
@AllArgsConstructor
public enum UserStatusEnum {

    ONLINE(0, "在线"),
    OFFLINE(1, "离线");

    private final Integer code;
    private final String desc;

}
