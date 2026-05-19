package com.shop.merchant.feign.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 卡密分配结果DTO
 * <p>
 * 支付成功后从shop-kami返回的卡密明文信息，
 * 包含卡密条目的ID和明文内容。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Data
public class KamiAllocateResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 卡密条目ID */
    private Long kamiItemId;

    /** 卡密明文内容 */
    private String plainContent;
}
