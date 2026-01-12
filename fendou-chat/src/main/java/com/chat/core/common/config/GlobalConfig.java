package com.chat.core.common.config;

import com.chat.core.common.interceptor.JwtInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目全局配置
 *
 * @author y
 * @since 2026-01-05
 */
@Configuration
public class GlobalConfig implements WebMvcConfigurer {

    /**
     * JWt 配置
     *
     * @param registry 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 将需要放行的路径添加到集合中
        List<String> list = new ArrayList<>();
        list.add("/auth/login");             // 登录
        list.add("/auth/captcha");           // 验证码
        list.add("/auth/register");          // 注册
        list.add("/uploads/**");             // 本地文件访问

        // 注册拦截器
        registry.addInterceptor(jwtInterceptor())
                // 拦截路径
                .addPathPatterns("/**")
                // 放行路径
                .excludePathPatterns(list);
    }
    
    /**
     * JwtInterceptor Bean定义
     * 
     * @return JwtInterceptor实例
     */
    @Bean
    public JwtInterceptor jwtInterceptor() {
        return new JwtInterceptor();
    }


    /**
     * 跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 所有接口
                .allowCredentials(true) // 是否发送 Cookie
                .allowedOriginPatterns("*") // 支持域
                .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS") // 支持方法
                .allowedHeaders("*")// 允许请求头
                .exposedHeaders("*");// 暴露出去的响应头
    }

}
