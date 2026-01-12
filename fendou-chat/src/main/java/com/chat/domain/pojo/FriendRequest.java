package com.chat.domain.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 好友请求实体类
 *
 * @author y
 * @since 2026-01-07
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class FriendRequest extends BasePOJO {
    private Integer senderId;
    private Integer receiverId;
    private String verifyMsg;
    private Integer status;
    
    // 覆盖父类的createTime字段，使用表中实际存在的字段
    private String createTime;
    
    // 标记父类中数据库不存在的字段
    @TableField(exist = false)
    private Integer createBy;
    
    @TableField(exist = false)
    private Integer updateBy;

    
    @TableField(exist = false)
    private String updateTime;
    
    @TableField(exist = false)
    private Integer isDeleted;
}
