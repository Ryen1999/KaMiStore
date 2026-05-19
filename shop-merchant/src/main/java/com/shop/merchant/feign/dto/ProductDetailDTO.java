package com.shop.merchant.feign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDetailDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long tenantId;

    private Long categoryId;

    private String productName;

    private Integer productType;

    private BigDecimal minPrice;

    private Integer totalStock;

    private Integer status;

    private List<ProductSkuDTO> skuList;
}
