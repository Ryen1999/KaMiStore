package com.shop.product.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 创建/编辑商品请求体
 * <p>
 * 组合了商品基本信息(ProductDTO)和SKU列表(ProductSkuDTO)，
 * 作为Controller层接收的统一请求对象。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Data
public class CreateProductRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 商品基本信息（必填） */
    @NotNull(message = "商品信息不能为空")
    @Valid
    private ProductDTO product;

    /** SKU列表 */
    @Valid
    private List<ProductSkuDTO> skuList;
}
