package com.chat.domain.dto.chat;

import lombok.Data;

/**
 * 聊天查询参数
 * @author y
 * @since 2026-01-07
 */

@Data
public class ChatQueryDTO {

    private String chatName;

    private Integer chatType;
}
