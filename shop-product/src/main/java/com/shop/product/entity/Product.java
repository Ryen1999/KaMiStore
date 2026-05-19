package com.shop.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_product")
public class Product extends BaseEntity {

    private Long tenantId;

    private Long categoryId;

    private transient String categoryName;

    private String productName;

    /**
     * 2 means card-secret product, the current default product type.
     */
    private Integer productType;

    private String mainImage;

    /**
     * JSON array.
     */
    private String images;

    private String detailHtml;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private Integer totalSales;

    /**
     * 0 draft, 1 pending, 2 online, 3 offline, 4 rejected.
     */
    private Integer status;

    private Integer sortOrder;

    private Integer isRecommend;
}
