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
import com.shop.product.dto.ProductVO;
import com.shop.product.entity.Product;
import com.shop.product.entity.ProductCategory;
import com.shop.product.entity.ProductSku;
import com.shop.product.mapper.ProductCategoryMapper;
import com.shop.product.mapper.ProductMapper;
import com.shop.product.mapper.ProductSkuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * 商品服务
 * <p>
 * 提供商品的核心业务功能：分页查询、详情查看、创建、编辑、删除、上下架。
 * 创建和编辑商品时会同步处理SKU数据，并自动计算价格区间和总库存。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    /** 商品Mapper */
    private final ProductMapper productMapper;

    /** SKU Mapper */
    private final ProductSkuMapper skuMapper;

    /** 分类Mapper */
    private final ProductCategoryMapper categoryMapper;

    /**
     * 分页查询商品列表
     * <p>
     * 支持按商品名称模糊搜索、分类筛选、状态筛选。
     * 结果按排序值升序、创建时间降序排列。
     * </p>
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    public PageResult<ProductVO> pageProducts(ProductQueryDTO queryDTO) {
        // 构建分页对象
        Page<Product> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        // 构建查询条件
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .like(StringUtils.hasText(queryDTO.getProductName()),
                        Product::getProductName, queryDTO.getProductName())
                .eq(queryDTO.getCategoryId() != null,
                        Product::getCategoryId, queryDTO.getCategoryId())
                .eq(queryDTO.getStatus() != null,
                        Product::getStatus, queryDTO.getStatus())
                .orderByAsc(Product::getSortOrder)
                .orderByDesc(Product::getCreatedAt);
        // 执行分页查询
        Page<Product> result = productMapper.selectPage(page, wrapper);
        
        // 转换为VO并查询分类名称
        List<ProductVO> voList = new java.util.ArrayList<>();
        for (Product product : result.getRecords()) {
            ProductVO vo = new ProductVO();
            BeanUtil.copyProperties(product, vo);
            // 查询分类名称
            if (product.getCategoryId() != null) {
                ProductCategory category = categoryMapper.selectById(product.getCategoryId());
                if (category != null) {
                    vo.setCategoryName(category.getCategoryName());
                }
            }
            voList.add(vo);
        }
        
        return PageResult.of(result.getTotal(), result.getCurrent(),
                result.getSize(), voList);
    }

    /**
     * 查询商品详情（包含SKU列表和分类名称）
     *
     * @param id 商品ID
     * @return 商品详情VO
     */
    public ProductVO getProductDetail(Long id) {
        // 查询商品基本信息
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException("商品不存在");
        }
        // 复制属性到VO
        ProductVO vo = new ProductVO();
        BeanUtil.copyProperties(product, vo);
        // 查询SKU列表
        List<ProductSku> skuList = skuMapper.selectList(
                new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, id)
                        .orderByAsc(ProductSku::getPrice)
        );
        vo.setSkuList(skuList);
        // 查询分类名称
        if (product.getCategoryId() != null) {
            ProductCategory category = categoryMapper.selectById(product.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getCategoryName());
            }
        }
        return vo;
    }

    /**
     * 创建商品和SKU
     * <p>
     * 自动计算minPrice、maxPrice、totalStock。
     * 商品默认状态为草稿(0)。
     * </p>
     *
     * @param dto     商品基本信息
     * @param skuList SKU列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void createProduct(ProductDTO dto, List<ProductSkuDTO> skuList) {
        // 构建商品实体
        Product product = new Product();
        BeanUtil.copyProperties(dto, product);
        product.setTenantId(TenantContext.getTenantId());
        // 默认商品类型为卡密(2)
        if (product.getProductType() == null) {
            product.setProductType(2);
        }
        // 默认状态为草稿
        product.setStatus(0);
        product.setTotalSales(0);
        // 确保images字段有合法的JSON默认值(MySQL JSON字段不接受空字符串)
        if (product.getImages() == null || product.getImages().trim().isEmpty()) {
            product.setImages("[]");
        }
        // 计算价格区间和总库存
        calculatePriceAndStock(product, skuList);
        // 插入商品
        productMapper.insert(product);
        // 批量插入SKU
        if (!CollectionUtils.isEmpty(skuList)) {
            for (ProductSkuDTO skuDto : skuList) {
                ProductSku sku = new ProductSku();
                BeanUtil.copyProperties(skuDto, sku);
                sku.setTenantId(TenantContext.getTenantId());
                sku.setProductId(product.getId());
                sku.setSales(0);
                sku.setStatus(1);
                skuMapper.insert(sku);
            }
        }
        log.info("创建商品成功, productId={}, productName={}", product.getId(), dto.getProductName());
    }

    /**
     * 更新商品和SKU
     * <p>
     * 采用先删除旧SKU再新增的策略，确保SKU数据一致性。
     * 自动重新计算minPrice、maxPrice、totalStock。
     * </p>
     *
     * @param id      商品ID
     * @param dto     商品基本信息
     * @param skuList SKU列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(Long id, ProductDTO dto, List<ProductSkuDTO> skuList) {
        // 校验商品是否存在
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException("商品不存在");
        }
        // 更新商品基本信息
        BeanUtil.copyProperties(dto, product);
        // 确保images字段有合法的JSON默认值(MySQL JSON字段不接受空字符串)
        if (product.getImages() == null || product.getImages().trim().isEmpty()) {
            product.setImages("[]");
        }
        // 重新计算价格区间和总库存
        calculatePriceAndStock(product, skuList);
        productMapper.updateById(product);
        // 删除旧SKU
        skuMapper.delete(
                new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, id)
        );
        // 插入新SKU
        if (!CollectionUtils.isEmpty(skuList)) {
            for (ProductSkuDTO skuDto : skuList) {
                ProductSku sku = new ProductSku();
                BeanUtil.copyProperties(skuDto, sku);
                sku.setTenantId(product.getTenantId());
                sku.setProductId(id);
                sku.setSales(0);
                sku.setStatus(1);
                skuMapper.insert(sku);
            }
        }
        log.info("更新商品成功, productId={}", id);
    }

    /**
     * 删除商品（逻辑删除）
     * <p>
     * 同时逻辑删除商品和对应的SKU数据。
     * </p>
     *
     * @param id 商品ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long id) {
        // 校验商品是否存在
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException("商品不存在");
        }
        // 逻辑删除商品
        productMapper.deleteById(id);
        // 逻辑删除对应的SKU
        skuMapper.delete(
                new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, id)
        );
        log.info("删除商品成功, productId={}", id);
    }

    /**
     * 更新商品状态（上架/下架）
     *
     * @param id     商品ID
     * @param status 目标状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        // 校验商品是否存在
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException("商品不存在");
        }
        product.setStatus(status);
        productMapper.updateById(product);
        log.info("更新商品状态成功, productId={}, status={}", id, status);
    }

    /**
     * 批量删除商品
     *
     * @param ids 商品ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        // 批量逻辑删除商品
        productMapper.deleteBatchIds(ids);
        // 批量逻辑删除对应的SKU
        skuMapper.delete(
                new LambdaQueryWrapper<ProductSku>()
                        .in(ProductSku::getProductId, ids)
        );
        log.info("批量删除商品成功, count={}", ids.size());
    }

    /**
     * 更新商品库存（增量方式）
     * <p>
     * 通过Feign远程调用，用于卡密导入后同步库存。
     * delta为正数表示增加库存，负数表示减少库存。
     * </p>
     *
     * @param productId 商品ID
     * @param delta     库存变化量
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStock(Long productId, Integer delta) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BizException("商品不存在");
        }
        int newStock = (product.getTotalStock() != null ? product.getTotalStock() : 0) + delta;
        if (newStock < 0) {
            throw new BizException("库存不足");
        }
        product.setTotalStock(newStock);
        productMapper.updateById(product);
        log.info("更新商品库存成功, productId={}, delta={}, newStock={}", productId, delta, newStock);
    }

    /**
     * 根据SKU列表计算商品的价格区间和总库存
     *
     * @param product 商品实体
     * @param skuList SKU列表
     */
    private void calculatePriceAndStock(Product product, List<ProductSkuDTO> skuList) {
        if (CollectionUtils.isEmpty(skuList)) {
            product.setMinPrice(BigDecimal.ZERO);
            product.setMaxPrice(BigDecimal.ZERO);
            product.setTotalStock(0);
            return;
        }
        // 计算最低价
        BigDecimal minPrice = skuList.stream()
                .map(ProductSkuDTO::getPrice)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        // 计算最高价
        BigDecimal maxPrice = skuList.stream()
                .map(ProductSkuDTO::getPrice)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        // 计算总库存
        int totalStock = skuList.stream()
                .map(s -> s.getStock() != null ? s.getStock() : 0)
                .reduce(0, Integer::sum);
        product.setMinPrice(minPrice);
        product.setMaxPrice(maxPrice);
        product.setTotalStock(totalStock);
    }
}
