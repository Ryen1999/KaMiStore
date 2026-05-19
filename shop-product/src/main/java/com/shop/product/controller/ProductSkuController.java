package com.shop.product.controller;

import com.shop.common.core.result.R;
import com.shop.product.entity.ProductSku;
import com.shop.product.service.ProductSkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product/sku")
@RequiredArgsConstructor
public class ProductSkuController {

    private final ProductSkuService productSkuService;

    @GetMapping("/list")
    public R<List<ProductSku>> listByProductId(@RequestParam Long productId) {
        return R.ok(productSkuService.listByProductId(productId));
    }
}
