package com.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chat.domain.pojo.Favorite;

import java.util.List;

/**
 * 收藏表Service接口
 *
 * @author y
 * @since 2026-01-07
 */
public interface FavoriteService extends IService<Favorite> {
    /**
     * 添加收藏
     *
     * @param favorite 收藏实体
     * @return 是否成功
     */
    boolean addFavorite(Favorite favorite);
    
    /**
     * 删除收藏
     *
     * @param id 收藏ID
     * @return 是否成功
     */
    boolean deleteFavorite(Integer id);
    
    /**
     * 根据收藏人ID获取收藏列表
     *
     * @param collectorId 收藏人ID
     * @return 收藏列表
     */
    List<Favorite> getFavoriteListByCollectorId(Integer collectorId);
    
    /**
     * 根据收藏人ID和搜索关键词获取收藏列表
     *
     * @param collectorId 收藏人ID
     * @param keyword 搜索关键词
     * @return 收藏列表
     */
    List<Favorite> searchFavorites(Integer collectorId, String keyword);
    
    /**
     * 检查是否已收藏
     *
     * @param collectorId 收藏人ID
     * @param collectedId 被收藏人ID
     * @param favoriteFrom 收藏来源
     * @return 是否已收藏
     */
    boolean isFavorite(Integer collectorId, Integer collectedId, Integer favoriteFrom);
}