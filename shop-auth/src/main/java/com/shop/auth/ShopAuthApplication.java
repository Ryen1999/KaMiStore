package com.shop.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 认证服务启动类
 * <p>
 * 提供统一的认证能力：
 * 1. 平台管理员登录（账号密码）
 * 2. 商家用户登录（账号密码）
 * 3. Token签发、刷新、注销
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = "com.shop")
@MapperScan("com.shop.auth.mapper")
public class ShopAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopAuthApplication.class, args);
    }
}
