package com.chat.domain.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息实体类
 *
 * @author y
 * @since 2026-01-07
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class Message extends BasePOJO {

    private String chatId;
    private String content;
    private String contentType;
    private Integer sendId;
    private Integer fileId;
    private Integer isRead;

    // 以下字段需执行 docs/db/upgrade_v2.sql 迁移后才持久化到DB
    // 是否已撤回：0-否，1-是
    @TableField(exist = false)
    private Integer isRecalled;
    // 定时发送时间（为null则立即发送）
    @TableField(exist = false)
    private String scheduledTime;
    // 消息回复的原消息ID
    @TableField(exist = false)
    private Integer replyMsgId;

    @TableField(exist = false)
    private String chatAvatar;

    @TableField(exist = false)
    private String realName;

    @TableField(exist = false)
    private String fileName;

    @TableField(exist = false)
    private Integer fileSize;

    @TableField(exist = false)
    private String fileType;

    @TableField(exist = false)
    private String replyContent;
}
