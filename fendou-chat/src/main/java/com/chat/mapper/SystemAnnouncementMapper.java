package com.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.domain.pojo.SystemAnnouncement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统公告 Mapper
 *
 * @author y
 * @since 2026-04-02
 */
@Mapper
public interface SystemAnnouncementMapper extends BaseMapper<SystemAnnouncement> {
}
