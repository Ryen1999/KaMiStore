package com.shop.kami;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 卡密服务启动类
 * <p>
 * 提供卡密核心能力：
 * 1. 卡密组管理（CRUD）
 * 2. 卡密导入（批量导入、去重、加密存储）
 * 3. 卡密库存同步（通过Feign同步SKU库存）
 * 4. 卡密作废与查询
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@ComponentScan("com.shop")
@MapperScan("com.shop.kami.mapper")
public class ShopKamiApplication {

    /**
     * 应用启动入口
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ShopKamiApplication.class, args);
    }
}
