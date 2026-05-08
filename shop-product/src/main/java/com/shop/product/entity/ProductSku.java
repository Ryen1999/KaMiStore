package com.shop.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品SKU实体
 * <p>
 * 对应数据库表t_product_sku，存储商品的规格信息。
 * 每个商品可以有多个SKU，每个SKU有独立的价格、库存、规格属性。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_product_sku")
public class ProductSku extends BaseEntity {

    /** 租户ID */
    private Long tenantId;

    /** 所属商品ID */
    private Long productId;

    /** SKU名称 */
    private String skuName;

    /** SKU图片URL */
    private String skuImage;

    /** 规格属性(JSON格式) */
    private String specJson;

    /** 销售价格 */
    private BigDecimal price;

    /** 原价（划线价） */
    private BigDecimal originalPrice;

    /** 成本价 */
    private BigDecimal costPrice;

    /** 库存数量 */
    private Integer stock;

    /** 销量 */
    private Integer sales;

    /** 卡密分组ID */
    private Long kamiGroupId;

    /** 重量(kg) */
    private BigDecimal weight;

    /** 状态：0禁用 1启用 */
    private Integer status;
}
