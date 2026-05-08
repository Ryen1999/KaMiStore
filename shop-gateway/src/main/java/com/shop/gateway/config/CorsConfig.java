package com.shop.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * 全局跨域配置
 * <p>
 * Gateway统一处理跨域，下游微服务不再单独配置CORS。
 * 允许所有来源、方法、请求头，满足前后端分离的跨域需求。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Configuration
public class CorsConfig {

    /**
     * 配置跨域过滤器
     *
     * @return CorsWebFilter（Gateway响应式跨域过滤器）
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有来源（生产环境建议限制为具体域名）
        config.addAllowedOriginPattern("*");
        // 允许所有请求方法
        config.addAllowedMethod("*");
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许携带Cookie
        config.setAllowCredentials(true);
        // 预检请求缓存时间（秒）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
