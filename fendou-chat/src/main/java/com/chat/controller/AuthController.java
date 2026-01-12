package com.chat.controller;

import com.chat.domain.dto.auth.LoginDTO;
import com.chat.domain.dto.user.UserRegisterDTO;
import com.chat.service.AuthService;
import com.chat.core.common.rersult.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @author y
 * @since 2026-01-07
 */

@RequestMapping("auth")
@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.ok(authService.login(loginDTO));
    }

    @GetMapping("/captcha")
    public Result<String> captcha(@RequestParam("randomStr") String randomStr) {
        return Result.ok(authService.captcha(randomStr));
    }


    @GetMapping("/info")
    public Result<?> info(@RequestHeader("Authorization") String token){
        return Result.ok(authService.getInfo(token));
    }

    @GetMapping("/logout")
    public Result<?> logout(@RequestHeader("Authorization") String token) {
        return authService.logout(token) ? Result.ok() : Result.fail();
    }

    @PostMapping("/register")
    public Result<Boolean> register(@Valid @RequestBody UserRegisterDTO userRegisterDTO) {
        return authService.register(userRegisterDTO) ? Result.ok() : Result.fail();
    }

}
