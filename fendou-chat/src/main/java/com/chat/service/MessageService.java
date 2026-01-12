package com.chat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chat.domain.pojo.Message;

/**
 * @author y
 * @since 2026-01-07
 */
public interface MessageService extends IService<Message> {

    Page<Message> getMessagesByChatId(String chatId, Integer pIndex, Integer pSize);


    Boolean updateReadStatus(String chatId, Integer userId);
}
