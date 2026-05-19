package com.shop.product.dto;

import lombok.Data;

@Data
public class ProductStockCountDTO {

    private Long productId;

    private Integer stock;
}
