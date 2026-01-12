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
}
