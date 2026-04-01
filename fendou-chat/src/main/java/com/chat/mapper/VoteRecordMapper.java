package com.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.domain.pojo.VoteRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 投票记录Mapper
 *
 * @author y
 * @since 2026-04-01
 */
@Mapper
public interface VoteRecordMapper extends BaseMapper<VoteRecord> {

    @Select("SELECT * FROM chat_vote_record WHERE vote_id = #{voteId} AND user_id = #{userId} AND is_deleted = 0")
    List<VoteRecord> selectByVoteAndUser(Integer voteId, Integer userId);

    @Select("SELECT option_id FROM chat_vote_record WHERE vote_id = #{voteId} AND is_deleted = 0")
    List<Integer> selectVotersByOption(Integer voteId);

    @Select("SELECT user_id FROM chat_vote_record WHERE vote_id = #{voteId} AND option_id = #{optionId} AND is_deleted = 0")
    List<Integer> selectVotersByOptionId(Integer voteId, Integer optionId);
}
