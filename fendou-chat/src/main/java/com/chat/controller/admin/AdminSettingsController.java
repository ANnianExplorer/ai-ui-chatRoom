package com.chat.controller.admin;

import com.chat.domain.dto.user.UserPasswordDTO;
import com.chat.domain.pojo.User;
import com.chat.service.UserService;
import com.chat.core.common.rersult.Result;
import com.chat.core.common.util.SecurityHolderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员设置控制器
 *
 * @author y
 * @since 2026-01-09
 */
@RequestMapping("/admin/settings")
@RestController
public class AdminSettingsController {

    @Autowired
    private UserService userService;

    /**
     * 获取系统设置
     *
     * @return 系统设置
     */
    @GetMapping
    public Result<?> getSettings() {
        // 这里需要添加系统设置相关服务
        // 暂时返回空对象，后续需要完善
        return Result.ok();
    }

    /**
     * 更新系统设置
     *
     * @param settings 系统设置
     * @return 是否更新成功
     */
    @PutMapping
    public Result<?> updateSettings(@RequestBody Object settings) {
        // 这里需要添加系统设置相关服务
        // 暂时返回成功，后续需要完善
        return Result.ok();
    }

    /**
     * 更新管理员信息
     *
     * @param userProfile 管理员信息
     * @return 是否更新成功
     */
    @PutMapping("/profile")
    public Result<?> updateAdminProfile(@RequestBody UserProfileRequest userProfile) {
        // 获取当前管理员用户ID
        Integer adminId = SecurityHolderUtils.getUserId();
        
        // 更新管理员信息
        User user = new User();
        user.setId(adminId);
        user.setRealName(userProfile.getRealName());
        user.setEmail(userProfile.getEmail());
        user.setPhone(userProfile.getPhone());
        if (userProfile.getAvatar() != null) {
            user.setAvatar(userProfile.getAvatar());
        }
        
        boolean updated = userService.updateById(user);
        return updated ? Result.ok() : Result.fail();
    }

    /**
     * 更新管理员密码
     *
     * @param passwordDTO 密码信息
     * @return 是否更新成功
     */
    @PutMapping("/password")
    public Result<?> updateAdminPassword(@RequestBody UserPasswordDTO passwordDTO) {
        // 这里可以复用用户密码更新的逻辑
        // 假设UserService的updatePassword方法已经处理了当前用户的密码更新
        boolean updated = userService.updatePassword(passwordDTO);
        return updated ? Result.ok() : Result.fail();
    }

    /**
     * 管理员信息更新请求DTO
     */
    public static class UserProfileRequest {
        private String realName;
        private String email;
        private String phone;
        private String avatar;

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }
    }
}
