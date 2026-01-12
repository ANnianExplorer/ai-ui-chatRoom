package com.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chat.domain.pojo.Message;
import com.chat.mapper.MessageMapper;
import com.chat.service.MessageService;
import org.springframework.stereotype.Service;

/**
 * @author y
 * @since 2026-01-07
 */

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Override
    public Page<Message> getMessagesByChatId(String chatId, Integer pIndex, Integer pSize) {
        Page<Message> page = new Page<>(pIndex, pSize);
        page.setRecords(baseMapper.selectMessagesByChatId(page, chatId));
        return page;
    }

    @Override
    public Boolean updateReadStatus(String chatId, Integer userId) {
        LambdaQueryWrapper<Message> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Message::getChatId, chatId);
        lqw.notIn(Message::getSendId, userId);

        Message message = new Message();
        message.setIsRead(1);

        return super.update(message, lqw);
    }
}
