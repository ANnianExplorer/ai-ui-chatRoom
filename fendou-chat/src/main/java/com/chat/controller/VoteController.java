package com.chat.controller;

import com.chat.core.common.rersult.Result;
import com.chat.core.common.util.SecurityHolderUtils;
import com.chat.domain.pojo.Vote;
import com.chat.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 投票控制器
 *
 * @author y
 * @since 2026-04-01
 */
@RestController
@RequestMapping("/vote")
public class VoteController {

    @Autowired
    private VoteService voteService;

    /**
     * 创建投票
     */
    @PostMapping("/create")
    public Result<?> createVote(@RequestBody Map<String, Object> body) {
        Integer userId = SecurityHolderUtils.getUserId();
        Vote vote = new Vote();
        vote.setChatId((String) body.get("chatId"));
        vote.setTitle((String) body.get("title"));
        vote.setDescription((String) body.get("description"));
        vote.setExpireTime((String) body.get("expireTime"));
        Object multiChoice = body.get("multiChoice");
        vote.setMultiChoice(multiChoice != null ? Integer.valueOf(multiChoice.toString()) : 0);
        Object anonymous = body.get("anonymous");
        vote.setAnonymous(anonymous != null ? Integer.valueOf(anonymous.toString()) : 0);

        @SuppressWarnings("unchecked")
        List<String> options = (List<String>) body.get("options");
        return Result.ok(voteService.createVote(vote, options, userId));
    }

    /**
     * 获取聊天室投票列表
     */
    @GetMapping("/list/{chatId}")
    public Result<?> getVotes(@PathVariable String chatId) {
        Integer userId = SecurityHolderUtils.getUserId();
        return Result.ok(voteService.getVotesByChatId(chatId, userId));
    }

    /**
     * 获取投票详情
     */
    @GetMapping("/{voteId}")
    public Result<?> getVoteDetail(@PathVariable Integer voteId) {
        Integer userId = SecurityHolderUtils.getUserId();
        return Result.ok(voteService.getVoteDetail(voteId, userId));
    }

    /**
     * 参与投票
     */
    @PostMapping("/{voteId}/vote")
    public Result<?> doVote(@PathVariable Integer voteId, @RequestBody Map<String, Object> body) {
        Integer userId = SecurityHolderUtils.getUserId();
        @SuppressWarnings("unchecked")
        List<Integer> optionIds = (List<Integer>) body.get("optionIds");
        return Result.ok(voteService.doVote(voteId, optionIds, userId));
    }
}
