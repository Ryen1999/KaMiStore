package com.shop.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.common.core.exception.BizException;
import com.shop.product.entity.Product;
import com.shop.product.entity.ProductSku;
import com.shop.product.mapper.ProductMapper;
import com.shop.product.mapper.ProductSkuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品SKU服务
 * <p>
 * 提供SKU的查询和库存管理功能。
 * 库存变更时会同步更新商品的总库存字段。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSkuService {

    /** SKU Mapper */
    private final ProductSkuMapper skuMapper;

    /** 商品Mapper（用于同步总库存） */
    private final ProductMapper productMapper;

    /**
     * 查询商品的所有SKU
     *
     * @param productId 商品ID
     * @return SKU列表
     */
    public List<ProductSku> listByProductId(Long productId) {
        return skuMapper.selectList(
                new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, productId)
                        .orderByAsc(ProductSku::getPrice)
        );
    }

    /**
     * 更新SKU库存（增加或减少）
     * <p>
     * delta为正数表示增加库存，负数表示减少库存。
     * 减少库存时会校验库存是否充足。
     * 操作完成后同步更新商品的totalStock字段。
     * </p>
     *
     * @param skuId SKU ID
     * @param delta 库存变化量（正数增加，负数减少）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStock(Long skuId, Integer delta) {
        // 查询SKU
        ProductSku sku = skuMapper.selectById(skuId);
        if (sku == null) {
            throw new BizException("SKU不存在");
        }
        // 计算新库存
        int newStock = (sku.getStock() != null ? sku.getStock() : 0) + delta;
        if (newStock < 0) {
            throw new BizException("库存不足");
        }
        // 更新SKU库存
        sku.setStock(newStock);
        skuMapper.updateById(sku);
        log.info("更新SKU库存成功, skuId={}, delta={}, newStock={}", skuId, delta, newStock);
        // 同步更新商品的总库存
        syncProductTotalStock(sku.getProductId());
    }

    /**
     * 同步商品总库存
     * <p>
     * 汇总该商品下所有SKU的库存，更新到商品的totalStock字段。
     * </p>
     *
     * @param productId 商品ID
     */
    private void syncProductTotalStock(Long productId) {
        // 查询该商品的所有SKU
        List<ProductSku> skuList = skuMapper.selectList(
                new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, productId)
        );
        // 汇总总库存
        int totalStock = skuList.stream()
                .map(s -> s.getStock() != null ? s.getStock() : 0)
                .reduce(0, Integer::sum);
        // 更新商品总库存
        Product product = productMapper.selectById(productId);
        if (product != null) {
            product.setTotalStock(totalStock);
            productMapper.updateById(product);
            log.info("同步商品总库存成功, productId={}, totalStock={}", productId, totalStock);
        }
    }
}
