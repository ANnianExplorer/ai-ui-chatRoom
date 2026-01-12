package com.chat.core.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 好友请求状体枚举
 *
 * @author y
 * @since 2026-01-07
 */

@Getter
@AllArgsConstructor
public enum FriendRequestStatusEnum {

    PENDING(0, "待处理"),
    AGREED(1, "已同意"),
    REFUSED(2, "已拒绝");

    private final Integer code;
    private final String desc;

}
