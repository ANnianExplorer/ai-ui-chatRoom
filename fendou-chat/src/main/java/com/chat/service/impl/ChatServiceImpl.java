package com.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.core.common.constant.CacheConstants;
import com.chat.core.common.constant.SecurityConstants;
import com.chat.domain.dto.chat.ChatQueryDTO;
import com.chat.domain.dto.user.UserQueryDTO;
import com.chat.domain.pojo.Group;
import com.chat.domain.pojo.Message;
import com.chat.domain.pojo.User;
import com.chat.domain.vo.ChatVO;
import com.chat.service.ChatService;
import com.chat.service.GroupService;
import com.chat.service.MessageService;
import com.chat.service.UserService;
import com.chat.core.common.util.SecurityHolderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author y
 * @since 2026-01-07
 */

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private UserService userService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private MessageService messageService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @Override
    public Page<ChatVO> getChats(Integer pIndex, Integer pSize, ChatQueryDTO chatQueryDTO) {
        List<ChatVO> chats = new ArrayList<>();

        Integer userId = SecurityHolderUtils.getUserId();
        Integer chatType = chatQueryDTO.getChatType();
        String chatName = chatQueryDTO.getChatName();

        UserQueryDTO userQueryDTO = new UserQueryDTO();
        userQueryDTO.setUserId(userId);
        Page<User> friends = userService.getFriends(0, -1, userQueryDTO);


        User user = (User) redisTemplate.opsForValue().get(CacheConstants.USER_KEY + SecurityHolderUtils.getToken());


        // 添加自己
        ChatVO self = new ChatVO();
        self.setChatId(generateChatId(userId, userId));
        self.setType(0);
        if (user != null) {
            self.setName(user.getUsername());
            self.setAvatar(user.getAvatar());
        }
        self.setRemark("自己");
        self.setDescription("我是你的分身");
        chats.add(self);

        // 添加AI
        ChatVO ai = new ChatVO();
        // 直接生成chatId，使用user-{userId}-0格式，与存储的数据一致
        ai.setChatId("user-" + userId + "-" + SecurityConstants.AI_USER_ID);
        ai.setType(3);
        ai.setName(SecurityConstants.AI_NAME);
        ai.setAvatar(SecurityConstants.AI_AVATAR);
        ai.setRemark(SecurityConstants.AI_REMARK);
        ai.setDescription(SecurityConstants.AI_DESCRIPTION);
        chats.add(ai);

        friends.getRecords().forEach(item -> {
            ChatVO vo = new ChatVO();
            vo.setChatId(generateChatId(SecurityHolderUtils.getUserId(), item.getId()));
            vo.setType(1);
            vo.setName(item.getRealName());
            vo.setAvatar(item.getAvatar());
            vo.setRemark(item.getRemark());
            vo.setDescription(item.getDescription());
            vo.setUnreadCount(item.getUnreadCount());

            chats.add(vo);
        });

        List<Group> groups = groupService.getGroupsByUserId(userId);
        groups.forEach(item -> {
            ChatVO vo = new ChatVO();
            vo.setChatId("group-" + item.getId());
            vo.setType(2);
            vo.setName(item.getName());
            vo.setAvatar(item.getAvatar());
            vo.setRemark(item.getUserRemark());
            vo.setDescription(item.getDescription());
            vo.setUnreadCount(item.getUnreadCount());

            chats.add(vo);
        });

        // 过滤条件
        List<ChatVO> filterChats = chats;
        if (chatName != null && !chatName.isEmpty()) {
            filterChats = chats.stream().filter(item -> item.getName().contains(chatName)).toList();
        }

        if (chatType != null && chatType != 0) {
            filterChats = chats.stream().filter(item -> item.getType().equals(chatType)).toList();
        }

        // 分页处理
        int total = filterChats.size();
        List<ChatVO> pagedChats = new ArrayList<>();

        if (pSize > 0 && total > 0) {
            int fromIndex = (pIndex - 1) * pSize;
            int toIndex = Math.min(fromIndex + pSize, total);

            // 检查索引是否有效
            if (fromIndex < total && fromIndex >= 0) {
                pagedChats = filterChats.subList(fromIndex, toIndex);
            }
        } else {
            pagedChats = filterChats;
        }

        Page<ChatVO> page = new Page<>();
        page.setRecords(pagedChats);
        page.setTotal(total);
        page.setCurrent(pIndex);
        page.setSize(pSize);

        return page;
    }

    @Override
    public Boolean clean(String chatId) {
        if (chatId != null && !chatId.isEmpty()) {
            messageService.remove(new LambdaQueryWrapper<Message>().eq(Message::getChatId, chatId));
        }

        return true;
    }

    public static String generateChatId(Integer userId, Integer friendId) {
        if (userId > friendId) {
            return "user-" + friendId + "-" + userId;
        } else {
            return "user-" + userId + "-" + friendId;
        }
    }
}
