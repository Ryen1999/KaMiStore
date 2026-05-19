package com.shop.merchant.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 支付宝预支付结果VO
 * <p>
 * 包含预支付订单号、二维码内容等，
 * 前端根据qrCode生成扫码图片。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Data
public class AlipayPrepayVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 平台订单号 */
    private String orderNo;

    /** 支付宝二维码链接（qrcode.vue可直接渲染） */
    private String qrCode;

    /** 支付宝交易号 */
    private String outTradeNo;

    /** 支付金额 */
    private BigDecimal amount;
}
