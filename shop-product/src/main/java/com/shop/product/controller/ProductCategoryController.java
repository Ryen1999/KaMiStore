package com.shop.product.controller;

import com.shop.common.core.result.R;
import com.shop.product.dto.CategoryDTO;
import com.shop.product.entity.ProductCategory;
import com.shop.product.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制器
 * <p>
 * 提供商品分类的增删改查接口。
 * 所有接口统一返回R对象。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@RestController
@RequestMapping("/product/category")
@RequiredArgsConstructor
public class ProductCategoryController {

    /** 分类服务 */
    private final ProductCategoryService categoryService;

    /**
     * 查询所有分类
     *
     * @return 分类列表
     */
    @GetMapping("/list")
    public R<List<ProductCategory>> listCategories() {
        return R.ok(categoryService.listCategories());
    }

    /**
     * 创建分类
     *
     * @param dto 分类信息
     * @return 操作结果
     */
    @PostMapping
    public R<Void> createCategory(@Validated @RequestBody CategoryDTO dto) {
        categoryService.createCategory(dto);
        return R.ok();
    }

    /**
     * 更新分类
     *
     * @param id  分类ID
     * @param dto 分类信息
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public R<Void> updateCategory(@PathVariable Long id,
                                  @Validated @RequestBody CategoryDTO dto) {
        categoryService.updateCategory(id, dto);
        return R.ok();
    }

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public R<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return R.ok();
    }
}
