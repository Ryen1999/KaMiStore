package com.shop.common.security.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token权限框架配置
 * <p>
 * 注册Sa-Token拦截器，拦截所有需要鉴权的请求。
 * 白名单路径（登录、注册、C端公开接口等）不拦截。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册Sa-Token路由拦截器
     * <p>
     * 拦截所有请求，排除登录/注册/文档等公开接口。
     * 通过注解方式在Controller方法上细化权限控制。
     * </p>
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 认证接口（登录/注册）
                        "/auth/**",
                        // C端公开接口（无需登录）
                        "/store/**",
                        // 接口文档
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v2/api-docs/**",
                        // 健康检查
                        "/actuator/**"
                );
    }
}
