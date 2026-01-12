package com.chat.controller;

import com.chat.domain.dto.user.UserPasswordDTO;
import com.chat.domain.dto.user.UserProfileDTO;
import com.chat.domain.dto.user.UserQueryDTO;
import com.chat.service.UserService;
import com.chat.core.common.rersult.Result;
import com.chat.core.common.util.SecurityHolderUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * @author y
 * @since 2026-01-07
 */
@RequestMapping("/user")
@RestController
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping("/friends")
    public Result<?> getFriends(@RequestParam(name = "pIndex", defaultValue = "1") Integer pIndex,
                                @RequestParam(name = "pSize", defaultValue = "10") Integer pSize,
                                @RequestBody UserQueryDTO userQueryDTO) {
        userQueryDTO.setUserId(SecurityHolderUtils.getUserId());
        return Result.ok(userService.getFriends(pIndex, pSize, userQueryDTO));
    }

    @GetMapping("/get/{id}")
    public Result<?> getById(@PathVariable("id") Integer id){
        return Result.ok(userService.getById(id));
    }

    @GetMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        return userService.removeById(id) ? Result.ok() : Result.fail();
    }


    @PostMapping("/update-profile")
    public Result<Boolean> updateProfile(@Valid @RequestBody UserProfileDTO userProfileDTO) {
        return userService.updateProfile(userProfileDTO) ? Result.ok() : Result.fail();
    }


    @PostMapping("/update-password")
    public Result<Boolean> updatePassword(@RequestBody UserPasswordDTO userPasswordDTO) {
        return userService.updatePassword(userPasswordDTO) ? Result.ok() : Result.fail();
    }

}
