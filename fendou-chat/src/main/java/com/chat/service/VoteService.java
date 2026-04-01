package com.chat.service;

import com.chat.domain.pojo.Vote;

import java.util.List;
import java.util.Map;

/**
 * 投票服务接口
 *
 * @author y
 * @since 2026-04-01
 */
public interface VoteService {

    /**
     * 创建投票
     */
    Vote createVote(Vote vote, List<String> options, Integer userId);

    /**
     * 获取聊天室投票列表
     */
    List<Vote> getVotesByChatId(String chatId, Integer userId);

    /**
     * 获取投票详情
     */
    Vote getVoteDetail(Integer voteId, Integer userId);

    /**
     * 参与投票
     */
    Vote doVote(Integer voteId, List<Integer> optionIds, Integer userId);
}
