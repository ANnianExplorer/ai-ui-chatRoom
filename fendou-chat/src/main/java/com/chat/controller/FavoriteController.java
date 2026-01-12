package com.chat.controller;

import com.chat.domain.pojo.Favorite;
import com.chat.service.FavoriteService;
import com.chat.core.common.rersult.Result;
import com.chat.core.common.util.SecurityHolderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收藏控制器
 *
 * @author y
 * @since 2026-01-07
 */
@RequestMapping("/favorite")
@RestController
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 添加收藏
     *
     * @param favorite 收藏实体
     * @return 是否成功
     */
    @PostMapping("/add")
    public Result<Boolean> addFavorite(@RequestBody Favorite favorite) {
        // 设置收藏人ID为当前登录用户
        favorite.setCollectorId(SecurityHolderUtils.getUserId());
        return favoriteService.addFavorite(favorite) ? Result.ok() : Result.fail();
    }

    /**
     * 删除收藏
     *
     * @param id 收藏ID
     * @return 是否成功
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> deleteFavorite(@PathVariable Integer id) {
        return favoriteService.deleteFavorite(id) ? Result.ok() : Result.fail();
    }

    /**
     * 获取当前用户的收藏列表
     *
     * @return 收藏列表
     */
    @GetMapping("/list")
    public Result<List<Favorite>> getFavoriteList() {
        Integer userId = (SecurityHolderUtils.getUserId());
        return Result.ok(favoriteService.getFavoriteListByCollectorId(userId));
    }

    /**
     * 搜索收藏
     *
     * @param keyword 搜索关键词
     * @return 收藏列表
     */
    @GetMapping("/search")
    public Result<List<Favorite>> searchFavorites(@RequestParam String keyword) {
        Integer userId = (SecurityHolderUtils.getUserId());
        return Result.ok(favoriteService.searchFavorites(userId, keyword));
    }

    /**
     * 检查是否已收藏
     *
     * @param collectedId 被收藏人ID
     * @param favoriteFrom 收藏来源
     * @return 是否已收藏
     */
    @GetMapping("/check")
    public Result<Boolean> checkFavorite(@RequestParam Integer collectedId, @RequestParam Integer favoriteFrom) {
        Integer userId = (SecurityHolderUtils.getUserId());
        return Result.ok(favoriteService.isFavorite(userId, collectedId, favoriteFrom));
    }
}