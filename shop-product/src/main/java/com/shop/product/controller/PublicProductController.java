package com.shop.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.common.core.result.PageResult;
import com.shop.common.core.result.R;
import com.shop.product.dto.ProductCategoryVO;
import com.shop.product.dto.ProductVO;
import com.shop.product.entity.Product;
import com.shop.product.entity.ProductCategory;
import com.shop.product.mapper.ProductCategoryMapper;
import com.shop.product.mapper.ProductMapper;
import com.shop.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping({"/public/product", "/api/public/product"})
@RequiredArgsConstructor
public class PublicProductController {

    private final ProductMapper productMapper;
    private final ProductCategoryMapper categoryMapper;
    private final ProductService productService;

    @GetMapping("/categories")
    public R<List<ProductCategoryVO>> getCategories(@RequestParam Long tenantId) {
        List<ProductCategory> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<ProductCategory>()
                        .eq(ProductCategory::getTenantId, tenantId)
                        .eq(ProductCategory::getStatus, 1)
                        .orderByAsc(ProductCategory::getSortOrder)
        );

        List<ProductCategoryVO> result = new ArrayList<>();
        for (ProductCategory category : categories) {
            Long count = productMapper.selectCount(
                    new LambdaQueryWrapper<Product>()
                            .eq(Product::getTenantId, tenantId)
                            .eq(Product::getCategoryId, category.getId())
                            .eq(Product::getStatus, 2)
            );

            ProductCategoryVO item = new ProductCategoryVO();
            item.setId(category.getId());
            item.setCategoryName(category.getCategoryName());
            item.setIcon(category.getIcon());
            item.setSortOrder(category.getSortOrder());
            item.setPageStyle(category.getPageStyle());
            item.setProductCount(count != null ? count : 0);
            result.add(item);
        }

        return R.ok(result);
    }

    @GetMapping("/list")
    public R<PageResult<ProductVO>> getProducts(@RequestParam Long tenantId,
                                                @RequestParam(required = false) Long categoryId,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(defaultValue = "1") Integer pageNum,
                                                @RequestParam(defaultValue = "20") Integer pageSize) {
        return R.ok(productService.pagePublicProducts(tenantId, categoryId, keyword, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public R<ProductVO> getProductDetail(@PathVariable Long id) {
        return R.ok(productService.getProductDetail(id));
    }
}
