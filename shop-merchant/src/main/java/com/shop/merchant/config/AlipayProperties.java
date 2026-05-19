package com.shop.merchant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝支付配置属性
 * <p>
 * 绑定Nacos配置中心shop.alipay前缀的配置项，
 * 包含沙箱网关地址、应用ID、签名密钥、回调地址等。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "shop.alipay")
public class AlipayProperties {

    /** 支付宝网关地址（沙箱环境） */
    private String gateway;

    /** 应用ID（沙箱APPID） */
    private String appId;

    /** 应用私钥（PKCS8格式） */
    private String privateKey;

    /** 支付宝公钥 */
    private String publicKey;

    /** 签名算法类型，默认RSA2 */
    private String signType = "RSA2";

    /** 编码格式，默认UTF-8 */
    private String charset = "UTF-8";

    /** 异步通知URL（平台回调地址） */
    private String notifyUrl;

    /** 同步跳转URL（支付完成后跳转的前端页面） */
    private String returnUrl;
}
