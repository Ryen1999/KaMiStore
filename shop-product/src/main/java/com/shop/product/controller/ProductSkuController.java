package com.shop.product.controller;

import com.shop.common.core.result.R;
import com.shop.product.entity.ProductSku;
import com.shop.product.service.ProductSkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品SKU控制器
 * <p>
 * 提供SKU的查询和库存管理接口。
 * 库存更新接口供卡密服务通过Feign远程调用。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@RestController
@RequestMapping("/product/sku")
@RequiredArgsConstructor
public class ProductSkuController {

    /** SKU服务 */
    private final ProductSkuService productSkuService;

    /**
     * 查询商品的所有SKU
     *
     * @param productId 商品ID
     * @return SKU列表
     */
    @GetMapping("/list")
    public R<List<ProductSku>> listByProductId(@RequestParam Long productId) {
        return R.ok(productSkuService.listByProductId(productId));
    }

    /**
     * 更新SKU库存
     * <p>
     * 供内部Feign调用，通过增量方式更新SKU库存。
     * delta为正数表示增加库存，负数表示减少库存。
     * </p>
     *
     * @param skuId SKU ID
     * @param delta 库存变化量
     * @return 操作结果
     */
    @PutMapping("/stock")
    public R<Void> updateStock(@RequestParam Long skuId, @RequestParam Integer delta) {
        productSkuService.updateStock(skuId, delta);
        return R.ok();
    }
}
