package com.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.domain.pojo.Vote;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 投票Mapper
 *
 * @author y
 * @since 2026-04-01
 */
@Mapper
public interface VoteMapper extends BaseMapper<Vote> {

    @Select("SELECT * FROM chat_vote WHERE chat_id = #{chatId} AND is_deleted = 0 ORDER BY create_time DESC")
    List<Vote> selectByChatId(String chatId);
}
