package com.chat.domain.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 收藏表实体类
 *
 * @author y
 * @since 2026-01-09
 */
@Data
@TableName("chat_favorite")
@EqualsAndHashCode(callSuper = true)
public class Favorite extends BasePOJO {
    /**
     * 收藏人ID
     */
    private Integer collectorId;
    
    /**
     * 被收藏人ID
     */
    private Integer collectedId;
    
    /**
     * 收藏来源(1:用户,2:群聊)
     */
    private Integer favoriteFrom;
    
    /**
     * 收藏类型(1:text,2:图片,3:文档)
     */
    private Integer favoriteType;

    /**
     * 收藏内容
     * 也就是收藏的内容的id，这样就可以联合数据库查看信息了
     * 分别根据收藏类型，查找对应的消息表、文件表
     */
    private Integer content;


    // 如果有status字段，需要确认数据库表中是否存在该字段
// 如果不存在，使用 @TableField(exist = false) 标记
    @TableField(exist = false)
    private Integer status;

    @TableField(exist = false)
    private Integer updateBy;
}