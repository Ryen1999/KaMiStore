package com.shop.merchant.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 支付方式配置更新DTO
 *
 * @author shop
 * @since 1.0.0
 */
@Data
public class PaymentMethodConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 商户号 */
    private String merchantId;

    /** API密钥 */
    private String apiKey;

    /** 私钥 */
    private String privateKey;
}