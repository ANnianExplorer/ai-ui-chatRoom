package com.chat.domain.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 投票记录实体类
 *
 * @author y
 * @since 2026-04-01
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class VoteRecord extends BasePOJO {

    // 投票ID
    private Integer voteId;

    // 选项ID
    private Integer optionId;

    // 投票用户ID
    private Integer userId;
}
