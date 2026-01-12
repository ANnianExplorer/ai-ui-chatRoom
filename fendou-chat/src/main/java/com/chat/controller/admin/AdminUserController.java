package com.chat.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.domain.dto.user.UserPasswordDTO;
import com.chat.domain.dto.user.UserProfileDTO;
import com.chat.domain.pojo.User;
import com.chat.service.UserService;
import com.chat.core.common.rersult.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员用户控制器
 *
 * @author y
 * @since 2026-01-09
 */
@RequestMapping("/admin/users")
@RestController
public class AdminUserController {

    @Autowired
    private UserService userService;

    /**
     * 获取所有用户列表
     *
     * @param page     当前页码
     * @param pageSize 每页大小
     * @param keyword  搜索关键词
     * @param status   用户状态
     * @return 用户列表
     */
    @GetMapping
    public Result<?> getUsers(@RequestParam(name = "page", defaultValue = "1") Integer page,
                             @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize,
                             @RequestParam(name = "keyword", required = false) String keyword,
                             @RequestParam(name = "status", required = false) String status) {
        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 处理搜索关键词
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(User::getUsername, keyword)
                       .or().like(User::getEmail, keyword)
                       .or().like(User::getPhone, keyword);
        }
        
        // 处理状态筛选
        if (status != null && !status.isEmpty()) {
            try {
                Integer statusInt = Integer.parseInt(status);
                queryWrapper.eq(User::getStatus, statusInt);
            } catch (NumberFormatException e) {
                // 忽略格式错误，不进行状态筛选
            }
        }
        
        // 执行分页查询
        Page<User> userPage = userService.page(new Page<>(page, pageSize), queryWrapper);
        
        // 转换为前端期望的格式
        Map<String, Object> result = new HashMap<>();
        result.put("list", userPage.getRecords());
        result.put("total", userPage.getTotal());
        
        return Result.ok(result);
    }

    /**
     * 获取单个用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public Result<?> getUserById(@PathVariable("id") Integer id) {
        User user = userService.getById(id);
        return Result.ok(user);
    }

    /**
     * 获取管理员信息
     */
    @GetMapping("/info")
    public Result<?> getAdminInfo() {
        User adminInfo = userService.getById(1);
        return Result.ok(adminInfo);
    }

    /**
     * 更新用户状态
     *
     * @param id     用户ID
     * @param status 状态（active/disabled）
     * @return 是否更新成功
     */
    @PutMapping("/{id}/status")
    public Result<?> updateUserStatus(@PathVariable("id") Integer id,
                                     @RequestBody StatusUpdateRequest status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status.getStatus());
        boolean updated = userService.updateById(user);
        return updated ? Result.ok() : Result.fail();
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteUser(@PathVariable("id") Integer id) {
        boolean deleted = userService.removeById(id);
        return deleted ? Result.ok() : Result.fail();
    }

    /**
     * 更新管理员信息
     */
    @PutMapping("/profile")
    public Result<Boolean> updateProfile(@Valid @RequestBody UserProfileDTO userProfileDTO) {
        userProfileDTO.setId(1);
        return userService.updateProfile(userProfileDTO) ? Result.ok() : Result.fail();
    }
    @PostMapping("/update-password")
    public Result<Boolean> updatePassword(@RequestBody UserPasswordDTO userPasswordDTO) {
        return userService.updatePassword(userPasswordDTO) ? Result.ok() : Result.fail();
    }

    /**
     * 状态更新请求DTO
     */
    public static class StatusUpdateRequest {
        private Integer status;

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }
}
