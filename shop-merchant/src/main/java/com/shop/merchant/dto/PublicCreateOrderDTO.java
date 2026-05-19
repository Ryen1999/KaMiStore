package com.shop.merchant.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PublicCreateOrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String shopCode;

    private Long productId;

    private Long skuId;

    private Integer quantity;

    private Integer contactType;

    private String contactValue;

    private String paymentMethod;
}
