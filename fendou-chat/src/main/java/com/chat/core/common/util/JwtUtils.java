package com.chat.core.common.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.chat.core.common.constant.SecurityConstants;

import java.util.*;

/**
 * jwt 工具类
 * @author y
 * @since 2026-01-05
 */
public class JwtUtils {
    private static Set<String> blackList = new HashSet<>();

    public Set<String> getBlackList() {
        return blackList;
    }

    public void setBlackList(Set<String> blackList) {
        JwtUtils.blackList = blackList;
    }

    /**
     * 创建 token
     *
     * @param claimMap 载体
     * @return token
     */
    public static String createToken(Map<String, String> claimMap) {
        // 过期时间=当前时间加设定时间
        Date expiration = new Date(System.currentTimeMillis() + SecurityConstants.EXPIRE_TIME);

        // 设置 JWT 头部
        Map<String, Object> map = new HashMap<>();
        map.put("typ", "JWT");
        map.put("alg", "HS256");

        // 创建 token
        JWTCreator.Builder builder = JWT.create();

        // 创建 payload 载体
        claimMap.forEach(builder::withClaim);

        // 生成 token
        return builder.withHeader(map)      // 设置头部
                .withExpiresAt(expiration)  // 设置过期时间
                .sign(Algorithm.HMAC256(SecurityConstants.SECRET)); // 设置签名解密算法
    }


    /**
     * 验证 token
     *
     * @param token token
     * @return 解密后的 token
     */
    public static DecodedJWT verifyToken(String token) {
        return JWT.require(Algorithm.HMAC256(SecurityConstants.SECRET)).build().verify(token);
    }


    /**
     * 添加到黑名单
     *
     * @param token token
     */
    public static void addBlacklist(String token) {
        blackList.add(token);
    }

    /**
     * 是否在黑名单
     *
     * @param token token
     * @return 是否在黑名单
     */
    public static boolean isBlacklist(String token) {
        return blackList.contains(token);
    }

    public static void main(String[] args) {
        System.out.println("========== 加密 token ==========");
        Map<String, String> map = new HashMap<>();
        map.put("userId", "1");
        map.put("username", "admin");
        map.put("password", "123456");
        String token = createToken(map);
        System.out.println(token);

        System.out.println("\n========== 解密 token ==========");
        DecodedJWT decoded = verifyToken(token);
        Claim userId = decoded.getClaims().get("userId");

        System.out.println(Integer.parseInt(userId.asString()));

        decoded.getClaims().forEach((k, v) -> System.out.println(k + ": " + v));
    }
}
