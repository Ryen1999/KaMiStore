package com.shop.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.common.core.context.TenantContext;
import com.shop.common.core.exception.BizException;
import com.shop.product.entity.ProductSku;
import com.shop.product.mapper.ProductSkuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSkuService {

    private final ProductSkuMapper skuMapper;

    public List<ProductSku> listByProductId(Long productId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户信息缺失");
        }
        return skuMapper.selectList(new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getTenantId, tenantId)
                .eq(ProductSku::getProductId, productId)
                .orderByAsc(ProductSku::getPrice));
    }
}
