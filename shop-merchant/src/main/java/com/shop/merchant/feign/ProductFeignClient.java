package com.shop.merchant.feign;

import com.shop.common.core.result.R;
import com.shop.merchant.feign.dto.ProductDetailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "shop-product", path = "/public/product")
public interface ProductFeignClient {

    @GetMapping("/{id}")
    R<ProductDetailDTO> getProductDetail(@RequestHeader("X-Tenant-Id") Long tenantId,
                                         @PathVariable("id") Long id);

    @PutMapping("/internal/stock")
    R<Void> updateStock(@RequestHeader("X-Internal-Token") String token,
                        @RequestHeader("X-Tenant-Id") Long tenantId,
                        @RequestParam("productId") Long productId,
                        @RequestParam("delta") Integer delta);
}
