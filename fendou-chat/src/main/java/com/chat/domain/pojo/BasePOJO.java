package com.chat.domain.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;

/**
 * 基础实体类
 *
 * @author y
 * @since 2026-01-07
 */

@Data
public class BasePOJO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键
    @TableId(type = IdType.AUTO)
    private Integer id;

    // 状态
    private Integer status;

    // 创建人
    private Integer createBy;

    // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String createTime;

    // 修改人
    private Integer updateBy;

    // 修改时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String updateTime;

    // 是否删除
    @TableLogic
    private Integer isDeleted;
}
