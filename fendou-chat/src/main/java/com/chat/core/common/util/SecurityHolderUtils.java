package com.chat.core.common.util;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

/**
 * 当前登录用户信息工具类
 *
 * @author y
 * @since 2026-01-05
 */
public class SecurityHolderUtils {


    /**
     * 获取 token
     */
    public static String getToken(){
        String authorization = getRequest().getHeader("Authorization");
        if (authorization == null || authorization.length() < 7) {
            return null;
        }
        return authorization.substring(7);
    }


    /**
     * 获取当前用户ID
     */
    public static Integer getUserId() {
        String token = getToken();
        if (token == null) {
            return null;
        }
        DecodedJWT verified = JwtUtils.verifyToken(token);
        return Integer.parseInt(verified.getClaim("id").asString());
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        String token = getToken();
        if (token == null) {
            return null;
        }
        DecodedJWT verified = JwtUtils.verifyToken(token);
        return verified.getClaim("username").asString();
    }

    /**
     * 获取当前请求
     */
    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
    }
}
