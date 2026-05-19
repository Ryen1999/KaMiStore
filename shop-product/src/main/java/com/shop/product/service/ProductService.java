package com.shop.product.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.common.core.context.TenantContext;
import com.shop.common.core.exception.BizException;
import com.shop.common.core.result.PageResult;
import com.shop.product.dto.ProductDTO;
import com.shop.product.dto.ProductQueryDTO;
import com.shop.product.dto.ProductSkuDTO;
import com.shop.product.dto.ProductStockCountDTO;
import com.shop.product.dto.ProductVO;
import com.shop.product.entity.Product;
import com.shop.product.entity.ProductCategory;
import com.shop.product.entity.ProductSku;
import com.shop.product.mapper.ProductCategoryMapper;
import com.shop.product.mapper.ProductMapper;
import com.shop.product.mapper.ProductSkuMapper;
import com.shop.product.mapper.ProductStockMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductSkuMapper skuMapper;
    private final ProductCategoryMapper categoryMapper;
    private final ProductStockMapper productStockMapper;

    public PageResult<ProductVO> pageProducts(ProductQueryDTO queryDTO) {
        Long tenantId = requireTenantId();
        Page<Product> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getTenantId, tenantId)
                .like(StringUtils.hasText(queryDTO.getProductName()), Product::getProductName, queryDTO.getProductName())
                .eq(queryDTO.getCategoryId() != null, Product::getCategoryId, queryDTO.getCategoryId())
                .eq(queryDTO.getStatus() != null, Product::getStatus, queryDTO.getStatus())
                .orderByAsc(Product::getSortOrder)
                .orderByDesc(Product::getCreatedAt);

        Page<Product> result = productMapper.selectPage(page, wrapper);
        return toProductPage(tenantId, result);
    }

    public PageResult<ProductVO> pagePublicProducts(Long tenantId, Long categoryId, String keyword,
                                                    Integer pageNum, Integer pageSize) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getTenantId, tenantId)
                .eq(Product::getStatus, 2)
                .eq(categoryId != null, Product::getCategoryId, categoryId)
                .like(StringUtils.hasText(keyword), Product::getProductName, keyword)
                .orderByAsc(Product::getSortOrder)
                .orderByDesc(Product::getCreatedAt);

        Page<Product> result = productMapper.selectPage(page, wrapper);
        return toProductPage(tenantId, result);
    }

    public ProductVO getProductDetail(Long id) {
        Long tenantId = requireTenantId();
        Product product = productMapper.selectOne(new LambdaQueryWrapper<Product>()
                .eq(Product::getId, id)
                .eq(Product::getTenantId, tenantId));
        if (product == null) {
            throw new BizException("商品不存在");
        }

        ProductVO vo = toProductVO(product, getAvailableStock(tenantId, id));
        List<ProductSku> skuList = skuMapper.selectList(new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getProductId, id)
                .eq(ProductSku::getTenantId, tenantId)
                .orderByAsc(ProductSku::getPrice));
        vo.setSkuList(skuList);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createProduct(ProductDTO dto, List<ProductSkuDTO> skuList) {
        Long tenantId = requireTenantId();
        Product product = new Product();
        BeanUtil.copyProperties(dto, product);
        product.setTenantId(tenantId);
        if (product.getProductType() == null) {
            product.setProductType(2);
        }
        product.setStatus(0);
        product.setTotalSales(0);
        if (product.getImages() == null || product.getImages().trim().isEmpty()) {
            product.setImages("[]");
        }
        calculatePriceRange(product, skuList);
        productMapper.insert(product);

        if (!CollectionUtils.isEmpty(skuList)) {
            for (ProductSkuDTO skuDto : skuList) {
                ProductSku sku = new ProductSku();
                BeanUtil.copyProperties(skuDto, sku);
                sku.setTenantId(tenantId);
                sku.setProductId(product.getId());
                sku.setSales(0);
                sku.setStatus(1);
                skuMapper.insert(sku);
            }
        }
        log.info("create product success, productId={}, productName={}", product.getId(), dto.getProductName());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(Long id, ProductDTO dto, List<ProductSkuDTO> skuList) {
        Long tenantId = requireTenantId();
        Product product = productMapper.selectOne(new LambdaQueryWrapper<Product>()
                .eq(Product::getId, id)
                .eq(Product::getTenantId, tenantId));
        if (product == null) {
            throw new BizException("商品不存在");
        }

        BeanUtil.copyProperties(dto, product);
        if (product.getImages() == null || product.getImages().trim().isEmpty()) {
            product.setImages("[]");
        }
        calculatePriceRange(product, skuList);
        productMapper.updateById(product);

        skuMapper.delete(new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getProductId, id)
                .eq(ProductSku::getTenantId, tenantId));

        if (!CollectionUtils.isEmpty(skuList)) {
            for (ProductSkuDTO skuDto : skuList) {
                ProductSku sku = new ProductSku();
                BeanUtil.copyProperties(skuDto, sku);
                sku.setTenantId(tenantId);
                sku.setProductId(id);
                sku.setSales(0);
                sku.setStatus(1);
                skuMapper.insert(sku);
            }
        }
        log.info("update product success, productId={}", id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long id) {
        Long tenantId = requireTenantId();
        Product product = productMapper.selectOne(new LambdaQueryWrapper<Product>()
                .eq(Product::getId, id)
                .eq(Product::getTenantId, tenantId));
        if (product == null) {
            throw new BizException("商品不存在");
        }

        productMapper.delete(new LambdaQueryWrapper<Product>()
                .eq(Product::getId, id)
                .eq(Product::getTenantId, tenantId));
        skuMapper.delete(new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getProductId, id)
                .eq(ProductSku::getTenantId, tenantId));
        log.info("delete product success, productId={}", id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Long tenantId = requireTenantId();
        Product product = productMapper.selectOne(new LambdaQueryWrapper<Product>()
                .eq(Product::getId, id)
                .eq(Product::getTenantId, tenantId));
        if (product == null) {
            throw new BizException("商品不存在");
        }
        product.setStatus(status);
        productMapper.updateById(product);
        log.info("update product status success, productId={}, status={}", id, status);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        Long tenantId = requireTenantId();
        if (ids == null || ids.isEmpty()) {
            return;
        }

        productMapper.delete(new LambdaQueryWrapper<Product>()
                .in(Product::getId, ids)
                .eq(Product::getTenantId, tenantId));
        skuMapper.delete(new LambdaQueryWrapper<ProductSku>()
                .in(ProductSku::getProductId, ids)
                .eq(ProductSku::getTenantId, tenantId));
        log.info("batch delete product success, count={}", ids.size());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStock(Long productId, Integer delta) {
        Long tenantId = requireTenantId();
        Product product = productMapper.selectOne(new LambdaQueryWrapper<Product>()
                .eq(Product::getId, productId)
                .eq(Product::getTenantId, tenantId));
        if (product == null) {
            throw new BizException("商品不存在");
        }
        log.info("skip product total stock update, stock is counted from kami items, productId={}, delta={}", productId, delta);
    }

    private PageResult<ProductVO> toProductPage(Long tenantId, Page<Product> result) {
        Map<Long, Integer> stockMap = getAvailableStockMap(tenantId, result.getRecords());
        List<ProductVO> voList = new ArrayList<>();
        for (Product product : result.getRecords()) {
            voList.add(toProductVO(product, stockMap.getOrDefault(product.getId(), 0)));
        }
        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), voList);
    }

    private ProductVO toProductVO(Product product, Integer totalStock) {
        ProductVO vo = new ProductVO();
        BeanUtil.copyProperties(product, vo);
        vo.setTotalStock(totalStock == null ? 0 : totalStock);
        vo.setTotalSales(product.getTotalSales() == null ? 0 : product.getTotalSales());
        if (product.getCategoryId() != null) {
            ProductCategory category = categoryMapper.selectById(product.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getCategoryName());
            }
        }
        return vo;
    }

    private void calculatePriceRange(Product product, List<ProductSkuDTO> skuList) {
        if (CollectionUtils.isEmpty(skuList)) {
            product.setMinPrice(BigDecimal.ZERO);
            product.setMaxPrice(BigDecimal.ZERO);
            return;
        }
        BigDecimal minPrice = skuList.stream()
                .map(ProductSkuDTO::getPrice)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        BigDecimal maxPrice = skuList.stream()
                .map(ProductSkuDTO::getPrice)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        product.setMinPrice(minPrice);
        product.setMaxPrice(maxPrice);
    }

    private Long requireTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("租户信息缺失");
        }
        return tenantId;
    }

    private Map<Long, Integer> getAvailableStockMap(Long tenantId, List<Product> products) {
        if (CollectionUtils.isEmpty(products)) {
            return java.util.Collections.emptyMap();
        }
        List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
        return productStockMapper.countAvailableStock(tenantId, productIds)
                .stream()
                .collect(Collectors.toMap(ProductStockCountDTO::getProductId, ProductStockCountDTO::getStock));
    }

    private Integer getAvailableStock(Long tenantId, Long productId) {
        Integer stock = productStockMapper.countAvailableStockByProductId(tenantId, productId);
        return stock == null ? 0 : stock;
    }
}
