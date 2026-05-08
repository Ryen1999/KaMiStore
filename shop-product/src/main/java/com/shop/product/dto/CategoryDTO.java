package com.shop.product.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 商品分类DTO
 * <p>
 * 用于接收前端提交的分类创建/编辑数据。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Data
public class CategoryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 父分类ID（0表示顶级分类） */
    private Long parentId;

    /** 分类名称（必填） */
    @NotBlank(message = "分类名称不能为空")
    private String categoryName;

    /** 分类图标URL */
    private String icon;

    /** 排序值 */
    private Integer sortOrder;

    /** 页面展示样式（card/list/grid） */
    private String pageStyle;
}
