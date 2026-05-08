package com.shop.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 网关服务启动类
 * <p>
 * 基于Spring Cloud Gateway实现API网关，提供：
 * 1. 路由转发：根据请求路径转发到对应微服务
 * 2. JWT鉴权：校验Token有效性
 * 3. 租户识别：从域名/请求头提取tenant_id
 * 4. 接口限流：Sentinel流量控制
 * 5. 跨域处理：CORS配置
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ShopGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopGatewayApplication.class, args);
    }
}
