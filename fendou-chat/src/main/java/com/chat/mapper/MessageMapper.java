package com.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.domain.pojo.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author y
 * @since 2026-01-07
 */

@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    List<Message> selectMessagesByChatId(Page<Message> page, @Param("chatId") String chatId);
}
