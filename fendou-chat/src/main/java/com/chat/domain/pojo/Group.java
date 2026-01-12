package com.chat.domain.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.w3c.dom.Text;

import java.util.List;

/**
 * 群聊实体类
 *
 * @author y
 * @since 2026-01-07
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class Group extends BasePOJO {

    private String name;

    private String avatar;

    private String remark;

    private String description;

    private Integer status;

    private String groupCall;

    @TableField(exist = false)
    private Integer unreadCount;


    @TableField(exist = false)
    private List<User> members;

    @TableField(exist = false)
    private String userRemark;
}
