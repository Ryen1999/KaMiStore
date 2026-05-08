package com.shop.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品分类实体
 * <p>
 * 对应数据库表t_product_category，支持多级分类结构。
 * 通过parentId字段实现父子关系，parentId为0表示顶级分类。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_product_category")
public class ProductCategory extends BaseEntity {

    /** 租户ID */
    private Long tenantId;

    /** 父分类ID（0表示顶级分类） */
    private Long parentId;

    /** 分类名称 */
    private String categoryName;

    /** 分类图标URL */
    private String icon;

    /** 排序值（越小越靠前） */
    private Integer sortOrder;

    /** 状态：0禁用 1启用 */
    private Integer status;

    /** 页面展示样式 */
    private String pageStyle;
}
