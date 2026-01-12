package com.chat.controller;

import com.chat.domain.dto.friend.FriendAddDTO;
import com.chat.domain.dto.friend.FriendQueryDTO;
import com.chat.domain.pojo.UserFriend;
import com.chat.service.FriendService;
import com.chat.core.common.rersult.Result;
import com.chat.core.common.util.SecurityHolderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 好友控制器
 *
 * @author y
 * @since 2026-01-07
 */

@RestController
@RequestMapping("/friend")
public class FriendController {

    @Autowired
    private FriendService friendService;


    @PostMapping("/add")
    public Result<?> addFriend(@RequestBody FriendAddDTO friendAddDTO) {
        return friendService.addFriend(friendAddDTO) ? Result.ok() : Result.fail();
    }

    @PutMapping("/respond")
    public Result<?> respondFriend(@RequestParam("requestId") Integer id, @RequestParam("status") Integer status) {
        return friendService.respondFriend(id, status) ? Result.ok() : Result.fail();
    }

    @PostMapping("/requests")
    public Result<?> deleteFriend(@RequestParam(name = "pIndex", defaultValue = "1") Integer pIndex,
                                  @RequestParam(name = "pSize", defaultValue = "10") Integer pSize,
                                  @RequestBody FriendQueryDTO friendQueryDTO) {
        return Result.ok(friendService.getFriendRequests(pIndex, pSize, friendQueryDTO));
    }

    @PutMapping("/update")
    public Result<?> updateFriend(@RequestBody UserFriend userFriend) {
        return friendService.updateFriend(userFriend) ? Result.ok() : Result.fail();
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> deleteFriend(@PathVariable("id") Integer id) {
        return friendService.deleteFriend(SecurityHolderUtils.getUserId(), id) ? Result.ok() : Result.fail();
    }

    @GetMapping("/req-count")
    public Result<?> getFriendCount() {
        return Result.ok(friendService.getFriendRequestCount());
    }

    @GetMapping("/verify/{friendId}")
    public Result<?> verifyIfFriend(@PathVariable("friendId") Integer friendId) {
        return Result.ok(friendService.verifyIfFriend(SecurityHolderUtils.getUserId(), friendId));
    }
}
