package com.chat.domain.vo;

import lombok.Data;

/**
 * @author y
 * @since 2026-01-07
 */

@Data
public class FriendRequestVO {
    private Integer id;
    private String senderName;
    private String verifyMsg;
    private Integer status;
    private String createTime;
}
