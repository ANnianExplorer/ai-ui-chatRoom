package com.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.domain.pojo.Favorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收藏表Mapper接口
 *
 * @author y
 * @since 2026-01-09
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}