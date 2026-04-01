package com.chat.domain.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 投票选项实体类
 *
 * @author y
 * @since 2026-04-01
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class VoteOption extends BasePOJO {

    // 投票ID
    private Integer voteId;

    // 选项内容
    private String content;

    // 投票数
    private Integer voteCount;

    @TableField(exist = false)
    private List<Integer> voterIds;

    @TableField(exist = false)
    private Boolean selected;
}
