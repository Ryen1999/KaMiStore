package com.shop.product.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 商品分页查询条件DTO
 * <p>
 * 用于接收商品列表查询的筛选条件和分页参数。
 * 支持按商品名称模糊搜索、分类筛选、状态筛选。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Data
public class ProductQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 商品名称（模糊搜索） */
    private String productName;

    /** 分类ID */
    private Long categoryId;

    /** 商品状态 */
    private Integer status;

    /** 当前页码（默认1） */
    private Integer pageNum = 1;

    /** 每页大小（默认10） */
    private Integer pageSize = 10;
}
