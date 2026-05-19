package com.shop.product.dto;

import com.shop.product.entity.ProductSku;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品详情VO
 * <p>
 * 用于返回商品详情信息，包含商品基本字段、SKU列表和分类名称。
 * 不继承Product实体，手动定义所有字段以解耦视图层与持久层。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Data
public class ProductVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 租户ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long tenantId;

    /** 分类ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long categoryId;

    /** 商品名称 */
    private String productName;

    /** 商品类型 */
    private Integer productType;

    /** 主图URL */
    private String mainImage;

    /** 商品图片集(JSON数组) */
    private String images;

    /** 商品详情(HTML富文本) */
    private String detailHtml;

    /** 最低价格 */
    private BigDecimal minPrice;

    /** 最高价格 */
    private BigDecimal maxPrice;

    /** 总库存 */
    private Integer totalStock;

    /** 总销量 */
    private Integer totalSales;

    /** 商品状态 */
    private Integer status;

    /** 排序值 */
    private Integer sortOrder;

    /** 是否推荐 */
    private Integer isRecommend;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** SKU列表 */
    private List<ProductSku> skuList;

    /** 分类名称 */
    private String categoryName;
}
