package com.shop.merchant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 商户服务启动类
 * <p>
 * 提供商户相关能力：
 * 1. 店铺配置管理（店铺名称、Logo、主题等）
 * 2. 商户角色管理（角色增删改查）
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = "com.shop")
@MapperScan("com.shop.merchant.mapper")
public class ShopMerchantApplication {

    /**
     * 商户服务启动入口
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ShopMerchantApplication.class, args);
    }
}
