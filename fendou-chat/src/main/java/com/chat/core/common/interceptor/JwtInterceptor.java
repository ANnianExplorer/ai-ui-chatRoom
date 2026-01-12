package com.chat.core.common.interceptor;

import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.chat.core.common.constant.CommonConstants;
import com.chat.core.common.util.JwtUtils;
import com.chat.core.common.util.SecurityHolderUtils;
import com.chat.domain.pojo.User;
import com.chat.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Jwt拦截器
 *
 * @author y
 * @since 2026-01-05
 */
public class JwtInterceptor implements HandlerInterceptor {
    
    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // options 请求不拦截
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 封装消息
        Map<String, Object> map = new HashMap<>();

        // 获取请求头中的 token
        String authorization = request.getHeader("Authorization");
        if (authorization != null) {
            map.put("message", "token 为空");

            String token = authorization.substring(7);

            try {
                // 验证令牌
                JwtUtils.verifyToken(token);

                // 判断是否在黑名单中
                if (JwtUtils.isBlacklist(token)) {
                    map.put("message", "token已被拉黑");
                } else {
                    // 获取当前用户ID
                    Integer userId = SecurityHolderUtils.getUserId();
                    // 查询用户信息
                    User user = userService.getById(userId);
                    // 检查用户状态，DISABLED_STATUS=0表示禁用状态
                    if (user != null && user.getStatus() == CommonConstants.DISABLED_STATUS) {
                        map.put("code", 401);
                        map.put("message", "账号已被禁用");
                        // 将token加入黑名单
                        JwtUtils.addBlacklist(token);
                    } else {
                        // 验证成功不拦截
                        return true;
                    }
                }
            } catch (SignatureVerificationException e) {
                map.put("message", "无效签名");
            } catch (TokenExpiredException e) {
                map.put("message", "token过期");
            } catch (AlgorithmMismatchException e) {
                map.put("message", "算法不匹配");
            } catch (Exception e) {
                map.put("message", "token无效");
            }
        }

        // 设置状态
        map.put("code", 401);

        // 将 map 转成 json
        String json = new ObjectMapper().writeValueAsString(map);

        // 设置响应编码格式
        response.setContentType("application/json;charset=utf-8");

        // 打印输出流
        PrintWriter pw = response.getWriter();

        // 输出数据
        pw.println(json);

        // 关闭流
        pw.close();

        // 拦截
        return false;
    }
}
