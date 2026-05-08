package com.shop.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品实体
 * <p>
 * 对应数据库表t_product，存储商品基本信息。
 * 商品状态流转：草稿(0) -> 待审核(1) -> 已上架(2) / 审核驳回(4)
 * 已上架(2) <-> 已下架(3)
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_product")
public class Product extends BaseEntity {

    /** 租户ID */
    private Long tenantId;

    /** 分类ID */
    private Long categoryId;

    /** 分类名称（非持久化字段） */
    private transient String categoryName;

    /** 商品名称 */
    private String productName;

    /** 商品类型：2=卡密（默认） */
    private Integer productType;

    /** 主图URL */
    private String mainImage;

    /** 商品图片集(JSON数组) */
    private String images;

    /** 商品详情(HTML富文本) */
    private String detailHtml;

    /** 最低价格（由SKU自动计算） */
    private BigDecimal minPrice;

    /** 最高价格（由SKU自动计算） */
    private BigDecimal maxPrice;

    /** 总库存（所有SKU库存之和） */
    private Integer totalStock;

    /** 总销量（所有SKU销量之和） */
    private Integer totalSales;

    /**
     * 商品状态
     * <ul>
     *   <li>0 - 草稿</li>
     *   <li>1 - 待审核</li>
     *   <li>2 - 已上架</li>
     *   <li>3 - 已下架</li>
     *   <li>4 - 审核驳回</li>
     * </ul>
     */
    private Integer status;

    /** 排序值（越小越靠前） */
    private Integer sortOrder;

    /** 是否推荐：0否 1是 */
    private Integer isRecommend;
}
