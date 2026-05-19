package com.shop.kami.feign;

import com.shop.common.core.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 商品远程调用客户端
 * <p>
 * 通过Feign调用shop-product服务的商品接口，
 * 主要用于在卡密导入后同步更新商品的库存数量。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@FeignClient(name = "shop-product", path = "/product")
public interface ProductFeignClient {

    /**
     * 更新商品库存
     * <p>
     * 通过增量方式更新商品总库存。
     * </p>
     *
     * @param productId 商品ID
     * @param delta     库存变化量（正数增加，负数减少）
     * @return 操作结果
     */
    @PutMapping("/stock")
    R<Void> updateStock(@RequestHeader("X-Tenant-Id") Long tenantId,
                        @RequestParam("productId") Long productId,
                        @RequestParam("delta") Integer delta);
}
