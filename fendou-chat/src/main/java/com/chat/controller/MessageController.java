package com.chat.controller;

import com.chat.service.MessageService;
import com.chat.core.common.rersult.Result;
import com.chat.core.common.util.SecurityHolderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 消息控制器
 *
 * @author y
 * @since 2026-01-07
 */

@RequestMapping("/message")
@RestController
public class MessageController {

    @Autowired
    private MessageService messageService;


    @GetMapping("/{chatId}")
    public Result<?> getMessageByChatId(@PathVariable("chatId") String chatId,
                                        @RequestParam(name = "pIndex", defaultValue = "1") Integer pIndex,
                                        @RequestParam(name = "pSize", defaultValue = "10") Integer pSize) {
        return Result.ok(messageService.getMessagesByChatId(chatId, pIndex, pSize));
    }

    @PostMapping("/markAsRead/{chatId}")
    public Result<?> markMarkAsRead(@PathVariable("chatId") String chatId ) {
        return Result.ok(messageService.updateReadStatus(chatId, SecurityHolderUtils.getUserId()));
    }

    @GetMapping("/detail/{id}")
    public Result<?> getMesg(@PathVariable("id") Integer id ) {
        return Result.ok(messageService.getById( id));
    }

}
