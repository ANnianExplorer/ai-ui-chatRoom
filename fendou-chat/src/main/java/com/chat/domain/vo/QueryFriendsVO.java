package com.chat.domain.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * @author y
 * @since 2026/4/2
 **/
@Data
public class QueryFriendsVO {
    private Integer friendId;

    private String friendName;

}
