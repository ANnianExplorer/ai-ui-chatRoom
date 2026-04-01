package com.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.domain.pojo.VoteOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 投票选项Mapper
 *
 * @author y
 * @since 2026-04-01
 */
@Mapper
public interface VoteOptionMapper extends BaseMapper<VoteOption> {

    @Select("SELECT * FROM chat_vote_option WHERE vote_id = #{voteId} AND is_deleted = 0")
    List<VoteOption> selectByVoteId(Integer voteId);

    @Update("UPDATE chat_vote_option SET vote_count = vote_count + 1 WHERE id = #{optionId}")
    int incrementVoteCount(Integer optionId);

    @Update("UPDATE chat_vote_option SET vote_count = vote_count - 1 WHERE id = #{optionId} AND vote_count > 0")
    int decrementVoteCount(Integer optionId);
}
