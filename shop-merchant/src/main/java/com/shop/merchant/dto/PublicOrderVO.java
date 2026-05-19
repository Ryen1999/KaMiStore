package com.shop.merchant.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PublicOrderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String orderNo;

    private String shopCode;

    private Integer status;

    private String productName;

    private String skuName;

    private Integer quantity;

    private BigDecimal amount;

    private String paymentMethod;

    private String payMode;

    private String payMessage;

    /** 支付宝预支付二维码链接 */
    private String qrCodeContent;

    /** 支付URL（用于唤醒支付宝App） */
    private String payUrl;

    /** 卡密明文列表（仅支付成功且联系方式校验通过后填充） */
    private java.util.List<String> kamiList;
}
