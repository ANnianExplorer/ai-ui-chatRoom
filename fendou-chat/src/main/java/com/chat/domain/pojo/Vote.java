package com.chat.domain.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 群投票实体类
 *
 * @author y
 * @since 2026-04-01
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Vote extends BasePOJO {

    // 所属聊天室ID
    private String chatId;

    // 投票标题
    private String title;

    // 投票描述
    private String description;

    // 过期时间
    private String expireTime;

    // 是否允许多选：0-否，1-是
    private Integer multiChoice;

    // 是否匿名：0-否，1-是
    private Integer anonymous;

    @TableField(exist = false)
    private List<VoteOption> options;

    @TableField(exist = false)
    private Boolean hasVoted;
}
