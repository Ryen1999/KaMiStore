package com.shop.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 商品服务启动类
 * <p>
 * 提供商品管理能力：
 * 1. 商品分类管理（增删改查）
 * 2. 商品信息管理（创建、编辑、上下架）
 * 3. SKU管理（规格、库存、价格）
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = "com.shop")
@MapperScan("com.shop.product.mapper")
public class ShopProductApplication {

    /**
     * 启动入口
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ShopProductApplication.class, args);
    }
}
