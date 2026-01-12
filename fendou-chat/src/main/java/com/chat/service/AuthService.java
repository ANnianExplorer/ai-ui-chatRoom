package com.chat.service;

import com.chat.domain.dto.auth.LoginDTO;
import com.chat.domain.dto.user.UserRegisterDTO;
import com.chat.domain.pojo.User;

import java.util.Map;

/**
 * @author y
 * @since 2026-01-07
 */
public interface AuthService {

    Map<String, String> login(LoginDTO loginDTO);

    Boolean register(UserRegisterDTO userRegisterDTO);

    String captcha(String randomStr);

    User getInfo(String token);

    Boolean logout(String token);
}
