package com.shop.product.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品SKU信息DTO
 * <p>
 * 用于接收前端提交的SKU规格数据，包含价格、库存、规格属性等。
 * 随商品创建/编辑一起提交。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Data
public class ProductSkuDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** SKU名称 */
    private String skuName;

    /** SKU图片URL */
    private String skuImage;

    /** 规格属性(JSON格式) */
    private String specJson;

    /** 销售价格（必填） */
    @NotNull(message = "SKU价格不能为空")
    private BigDecimal price;

    /** 原价（划线价） */
    private BigDecimal originalPrice;

    /** 成本价 */
    private BigDecimal costPrice;

    /** 库存数量 */
    private Integer stock;

    /** 卡密分组ID */
    private Long kamiGroupId;

    /** 重量(kg) */
    private BigDecimal weight;
}
