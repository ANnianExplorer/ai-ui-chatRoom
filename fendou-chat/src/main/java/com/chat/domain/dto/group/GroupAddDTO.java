package com.chat.domain.dto.group;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * @author y
 * @since 2026-01-07
 */
@Data
public class GroupAddDTO {

    private String name;
    private String remark;
    private String description;

}
