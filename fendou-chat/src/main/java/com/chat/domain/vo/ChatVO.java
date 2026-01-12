package com.chat.domain.vo;

import lombok.Data;

/**
 * @author y
 * @since 2026-01-07
 */

@Data
public class ChatVO {

    private String chatId;

    private Integer type;

    private String name;

    private String avatar;

    private String remark;

    private String description;

    private String createTime;

    private Integer unreadCount;

}
