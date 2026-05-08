package com.shop.product.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 商品创建/编辑DTO
 * <p>
 * 用于接收前端提交的商品基本信息，不包含SKU数据。
 * SKU数据通过CreateProductRequest一起提交。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Data
public class ProductDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 分类ID（必填） */
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    /** 商品名称（必填） */
    @NotBlank(message = "商品名称不能为空")
    private String productName;

    /** 商品类型：2=卡密（默认） */
    private Integer productType;

    /** 主图URL */
    private String mainImage;

    /** 商品图片集(JSON数组) */
    private String images;

    /** 商品详情(HTML富文本) */
    private String detailHtml;

    /** 排序值 */
    private Integer sortOrder;

    /** 是否推荐：0否 1是 */
    private Integer isRecommend;
}
