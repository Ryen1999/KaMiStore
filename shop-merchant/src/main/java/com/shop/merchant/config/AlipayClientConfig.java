package com.shop.merchant.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝客户端配置类
 * <p>
 * 创建并注入AlipayClient单例Bean，全局复用同一客户端实例。
 * 使用沙箱网关与沙箱应用私钥/支付宝公钥完成签名。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class AlipayClientConfig {

    /** 支付宝配置属性 */
    private final AlipayProperties alipayProperties;

    /**
     * 创建支付宝客户端Bean
     *
     * @return DefaultAlipayClient实例
     */
    @Bean
    public AlipayClient alipayClient() {
        return new DefaultAlipayClient(
                alipayProperties.getGateway(),
                alipayProperties.getAppId(),
                alipayProperties.getPrivateKey(),
                "JSON",
                alipayProperties.getCharset(),
                alipayProperties.getPublicKey(),
                alipayProperties.getSignType()
        );
    }
}
