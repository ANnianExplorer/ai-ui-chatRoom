package com.chat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.domain.dto.chat.ChatQueryDTO;
import com.chat.domain.vo.ChatVO;
import org.springframework.stereotype.Service;

/**
 * @author y
 * @since 2026-01-07
 */

public interface ChatService {


    Page<ChatVO> getChats(Integer pIndex, Integer pSize, ChatQueryDTO chatQueryDTO);

    Boolean clean(String chatId);
}
