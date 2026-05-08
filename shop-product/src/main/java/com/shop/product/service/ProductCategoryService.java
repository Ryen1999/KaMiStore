package com.shop.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.common.core.context.TenantContext;
import com.shop.common.core.exception.BizException;
import com.shop.product.dto.CategoryDTO;
import com.shop.product.entity.Product;
import com.shop.product.entity.ProductCategory;
import com.shop.product.mapper.ProductCategoryMapper;
import com.shop.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品分类服务
 * <p>
 * 提供商品分类的增删改查功能。
 * 所有操作基于当前租户上下文，确保数据隔离。
 * 删除分类前会检查是否有商品引用该分类。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCategoryService {

    /** 分类Mapper */
    private final ProductCategoryMapper categoryMapper;

    /** 商品Mapper（用于删除前检查引用） */
    private final ProductMapper productMapper;

    /**
     * 查询当前租户的所有分类
     *
     * @return 分类列表
     */
    public List<ProductCategory> listCategories() {
        // 根据排序值升序查询当前租户的所有分类
        Long tenantId = TenantContext.getTenantId();
        return categoryMapper.selectList(
                new LambdaQueryWrapper<ProductCategory>()
                        .eq(ProductCategory::getTenantId, tenantId)
                        .orderByAsc(ProductCategory::getSortOrder)
                        .orderByDesc(ProductCategory::getCreatedAt)
        );
    }

    /**
     * 创建分类
     *
     * @param dto 分类信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void createCategory(CategoryDTO dto) {
        ProductCategory category = new ProductCategory();
        // 设置租户ID
        category.setTenantId(TenantContext.getTenantId());
        category.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        category.setCategoryName(dto.getCategoryName());
        category.setIcon(dto.getIcon());
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        // 默认启用
        category.setStatus(1);
        // 页面样式，默认card
        category.setPageStyle(dto.getPageStyle() != null ? dto.getPageStyle() : "card");
        categoryMapper.insert(category);
        log.info("创建商品分类成功, categoryName={}", dto.getCategoryName());
    }

    /**
     * 更新分类
     *
     * @param id  分类ID
     * @param dto 分类信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Long id, CategoryDTO dto) {
        // 查询分类是否存在
        Long tenantId = TenantContext.getTenantId();
        ProductCategory category = categoryMapper.selectOne(
                new LambdaQueryWrapper<ProductCategory>()
                        .eq(ProductCategory::getId, id)
                        .eq(ProductCategory::getTenantId, tenantId)
        );
        if (category == null) {
            throw new BizException("分类不存在或无权操作");
        }
        // 更新字段
        if (dto.getParentId() != null) {
            category.setParentId(dto.getParentId());
        }
        category.setCategoryName(dto.getCategoryName());
        category.setIcon(dto.getIcon());
        if (dto.getSortOrder() != null) {
            category.setSortOrder(dto.getSortOrder());
        }
        // 更新页面样式
        if (dto.getPageStyle() != null) {
            category.setPageStyle(dto.getPageStyle());
        }
        categoryMapper.updateById(category);
        log.info("更新商品分类成功, id={}", id);
    }

    /**
     * 删除分类
     * <p>
     * 删除前检查是否有商品引用该分类，如果有则不允许删除。
     * </p>
     *
     * @param id 分类ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        Long tenantId = TenantContext.getTenantId();
        // 检查分类是否存在且属于当前租户
        ProductCategory category = categoryMapper.selectOne(
                new LambdaQueryWrapper<ProductCategory>()
                        .eq(ProductCategory::getId, id)
                        .eq(ProductCategory::getTenantId, tenantId)
        );
        if (category == null) {
            throw new BizException("分类不存在或无权操作");
        }
        // 检查是否有商品引用该分类
        Long count = productMapper.selectCount(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getCategoryId, id)
        );
        if (count > 0) {
            throw new BizException("该分类下存在商品，无法删除");
        }
        categoryMapper.deleteById(id);
        log.info("删除商品分类成功, id={}", id);
    }
}
