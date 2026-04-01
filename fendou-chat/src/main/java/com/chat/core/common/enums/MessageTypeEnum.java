package com.chat.core.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型枚举
 *
 * @author y
 * @since 2026-01-07
 */

@Getter
@AllArgsConstructor
public enum MessageTypeEnum {

    // 初始化用户状态消息
    INIT_USER_STATUS,

    // 群在线人数消息
    GROUP_ONLINE_COUNT,

    // 用户状态消息
    USER_STATUS,

    // 错误消息
    ERROR,

    // 心跳消息
    HEARTBEAT,

    // 心跳确认消息
    HEARTBEAT_ACK,

    // 用户消息
    USER,

    /**
     * REQUEST_USER_STATUS - 请求用户状态
     */
    REQUEST_USER_STATUS,
    
    /**
     * REQUEST_GROUP_ONLINE_COUNT - 请求群聊在线人数
     */
    REQUEST_GROUP_ONLINE_COUNT,

    // 群消息
    GROUP,

    // 消息撤回
    RECALL,

    // 正在输入
    TYPING,

    // 投票消息
    VOTE,

    // 投票操作
    VOTE_ACTION,

    // 位置消息
    LOCATION,

    // 系统公告
    SYSTEM_NOTICE,

    // 禁言通知
    MUTE_NOTICE,

    // 已读回执
    READ_RECEIPT,

    // 定时消息
    SCHEDULED;


    /**
     * 判断是否是某个消息类型
     *
     * @param type            消息类型
     * @param messageTypeEnum 消息类型枚举
     * @return boolean
     */
    public static Boolean isType(String type, MessageTypeEnum messageTypeEnum) {
        return type != null && type.equalsIgnoreCase(messageTypeEnum.name());
    }
    
    
}
