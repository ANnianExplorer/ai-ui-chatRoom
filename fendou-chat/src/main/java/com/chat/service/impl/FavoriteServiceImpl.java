package com.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chat.core.common.util.SecurityHolderUtils;
import com.chat.domain.pojo.Favorite;
import com.chat.domain.pojo.Message;
import com.chat.domain.pojo.File;
import com.chat.mapper.FavoriteMapper;
import com.chat.service.FavoriteService;
import com.chat.service.MessageService;
import com.chat.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏表Service实现类
 *
 * @author y
 * @since 2026-01-09
 */
@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private FileService fileService;


    @Override
    public boolean deleteFavorite(Integer id) {
        // 逻辑删除
        return removeById(id);
    }

    @Override
    public List<Favorite> getFavoriteListByCollectorId(Integer collectorId) {
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getCollectorId, collectorId)
                .eq(Favorite::getIsDeleted, 0)
                .orderByDesc(Favorite::getCreateTime);
        return list(queryWrapper);
    }

    @Override
    public List<Favorite> searchFavorites(Integer collectorId, String keyword) {
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getCollectorId, collectorId)
                .eq(Favorite::getIsDeleted, 0)
                .orderByDesc(Favorite::getCreateTime);
        // TODO: 这里需要根据关键词搜索，但是收藏表中没有直接的内容字段
        // 可能需要关联其他表，比如消息表、用户表等
        return list(queryWrapper);
    }

    @Override
    public boolean isFavorite(Integer collectorId, Integer collectedId, Integer favoriteFrom) {
        // 这个方法已经不符合新的业务逻辑，因为现在同一用户可以收藏同一对象的不同内容
        // 应该使用带content参数的重载方法
        // 为了向后兼容，这里可以查询是否有任何内容类型的收藏
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getCollectorId, collectorId)
                .eq(Favorite::getCollectedId, collectedId)
                .eq(Favorite::getFavoriteFrom, favoriteFrom)
                .eq(Favorite::getIsDeleted, 0);
        return count(queryWrapper) > 0;
    }
    
    /**
     * 检查是否已收藏（带content参数）
     *
     * @param collectorId 收藏人ID
     * @param collectedId 被收藏人ID
     * @param favoriteFrom 收藏来源
     * @param content 收藏内容ID
     * @return 是否已收藏
     */
    public boolean isFavorite(Integer collectorId, Integer collectedId, Integer favoriteFrom, Integer content) {
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getCollectorId, collectorId)
                .eq(Favorite::getCollectedId, collectedId)
                .eq(Favorite::getFavoriteFrom, favoriteFrom)
                .eq(Favorite::getContent, content)
                .eq(Favorite::getIsDeleted, 0);
        return count(queryWrapper) > 0;
    }
    
    @Override
    public boolean addFavorite(Favorite favorite) {
        // 检查是否已收藏
        if (isFavorite(favorite.getCollectorId(), favorite.getCollectedId(), favorite.getFavoriteFrom(), favorite.getContent())) {
            // 已收藏则返回true，表示收藏已完成（避免重复操作）
            return true;
        }
        // 设置默认值
        favorite.setIsDeleted(0);
        favorite.setCreateBy(SecurityHolderUtils.getUserId());
        return save(favorite);
    }
}