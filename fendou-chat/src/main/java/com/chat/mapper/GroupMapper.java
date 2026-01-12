package com.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.domain.pojo.Group;

import java.util.List;

/**
 * @author y
 * @since 2026-01-07
 */
public interface GroupMapper extends BaseMapper<Group> {

    List<Group> selectGroupsByUserId(Integer userId);

}
