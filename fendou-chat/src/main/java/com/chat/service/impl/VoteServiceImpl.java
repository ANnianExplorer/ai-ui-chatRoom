package com.chat.service.impl;

import com.chat.domain.pojo.Vote;
import com.chat.domain.pojo.VoteOption;
import com.chat.domain.pojo.VoteRecord;
import com.chat.mapper.VoteMapper;
import com.chat.mapper.VoteOptionMapper;
import com.chat.mapper.VoteRecordMapper;
import com.chat.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 投票服务实现类
 *
 * @author y
 * @since 2026-04-01
 */
@Service
public class VoteServiceImpl implements VoteService {

    @Autowired
    private VoteMapper voteMapper;

    @Autowired
    private VoteOptionMapper voteOptionMapper;

    @Autowired
    private VoteRecordMapper voteRecordMapper;

    @Override
    @Transactional
    public Vote createVote(Vote vote, List<String> options, Integer userId) {
        vote.setCreateBy(userId);
        vote.setStatus(1);
        vote.setIsDeleted(0);
        voteMapper.insert(vote);

        for (String optionContent : options) {
            VoteOption option = new VoteOption();
            option.setVoteId(vote.getId());
            option.setContent(optionContent);
            option.setVoteCount(0);
            option.setStatus(1);
            option.setIsDeleted(0);
            voteOptionMapper.insert(option);
        }

        return getVoteDetail(vote.getId(), userId);
    }

    @Override
    public List<Vote> getVotesByChatId(String chatId, Integer userId) {
        List<Vote> votes = voteMapper.selectByChatId(chatId);
        for (Vote vote : votes) {
            enrichVoteDetail(vote, userId);
        }
        return votes;
    }

    @Override
    public Vote getVoteDetail(Integer voteId, Integer userId) {
        Vote vote = voteMapper.selectById(voteId);
        if (vote == null) return null;
        enrichVoteDetail(vote, userId);
        return vote;
    }

    @Override
    @Transactional
    public Vote doVote(Integer voteId, List<Integer> optionIds, Integer userId) {
        Vote vote = voteMapper.selectById(voteId);
        if (vote == null) throw new RuntimeException("投票不存在");

        // 去重 optionIds（防止前端传入重复ID导致唯一索引冲突）
        List<Integer> uniqueOptionIds = optionIds.stream().distinct().toList();

        // 检查是否已投票，如果有则先减票数
        List<VoteRecord> existingRecords = voteRecordMapper.selectByVoteAndUser(voteId, userId);
        if (!existingRecords.isEmpty()) {
            for (VoteRecord record : existingRecords) {
                voteOptionMapper.decrementVoteCount(record.getOptionId());
            }
            // 物理删除旧记录（避免逻辑删除导致唯一索引冲突）
            voteRecordMapper.physicalDeleteByVoteAndUser(voteId, userId);
        }

        // 记录新投票
        for (Integer optionId : uniqueOptionIds) {
            VoteRecord record = new VoteRecord();
            record.setVoteId(voteId);
            record.setOptionId(optionId);
            record.setUserId(userId);
            record.setStatus(1);
            record.setIsDeleted(0);
            voteRecordMapper.insert(record);
            voteOptionMapper.incrementVoteCount(optionId);
        }

        return getVoteDetail(voteId, userId);
    }

    private void enrichVoteDetail(Vote vote, Integer userId) {
        List<VoteOption> options = voteOptionMapper.selectByVoteId(vote.getId());
        List<VoteRecord> userRecords = voteRecordMapper.selectByVoteAndUser(vote.getId(), userId);
        List<Integer> userVotedOptionIds = new ArrayList<>();
        for (VoteRecord record : userRecords) {
            userVotedOptionIds.add(record.getOptionId());
        }

        for (VoteOption option : options) {
            List<Integer> voterIds = voteRecordMapper.selectVotersByOptionId(vote.getId(), option.getId());
            option.setVoterIds(voterIds);
            option.setSelected(userVotedOptionIds.contains(option.getId()));
        }

        vote.setOptions(options);
        vote.setHasVoted(!userVotedOptionIds.isEmpty());
    }
}
