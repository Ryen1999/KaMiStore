package com.shop.merchant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.shop.merchant.feign")
@ComponentScan(basePackages = "com.shop")
@MapperScan("com.shop.merchant.mapper")
public class ShopMerchantApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopMerchantApplication.class, args);
    }
}
