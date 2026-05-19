package com.shop.product.controller;

import com.shop.common.core.exception.BizException;
import com.shop.common.core.result.R;
import com.shop.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/public/product/internal", "/api/public/product/internal"})
@RequiredArgsConstructor
public class InternalProductController {

    private final ProductService productService;

    @Value("${shop.internal-token:shop-internal}")
    private String internalToken;

    @PutMapping("/stock")
    public R<Void> updateStock(@RequestHeader("X-Internal-Token") String token,
                               @RequestParam Long productId,
                               @RequestParam Integer delta) {
        if (!internalToken.equals(token)) {
            throw new BizException("invalid internal token");
        }
        productService.updateStock(productId, delta);
        return R.ok();
    }
}
