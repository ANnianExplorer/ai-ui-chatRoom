package com.chat.controller;

import com.chat.domain.dto.chat.ChatQueryDTO;
import com.chat.service.ChatService;
import com.chat.core.common.rersult.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 聊天控制器
 *
 * @author y
 * @since 2026-01-07
 */

@RestController
@RequestMapping("chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/list")
    public Result<Object> chats(@RequestParam(name = "pIndex", defaultValue = "1") Integer pIndex,
                                @RequestParam(name = "pSize", defaultValue = "10") Integer pSize,
                                @RequestBody ChatQueryDTO chatQueryDTO) {
        return Result.ok(chatService.getChats(pIndex, pSize, chatQueryDTO));
    }


    @DeleteMapping("/clean/{chatId}")
    public Result<Object> clean(@PathVariable("chatId") String chatId) {
        return chatService.clean(chatId) ? Result.ok() : Result.fail();
    }

}
