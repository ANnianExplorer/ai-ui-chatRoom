package com.chat.domain.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author y
 * @since 2026-01-07
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class GroupMember extends BasePOJO {

    private Integer groupId;
    private Integer userId;
    private String remark;

    @TableField(exist = false)
    private Integer isDeleted;
}
