package com.shop.product.controller;

import com.shop.common.core.result.PageResult;
import com.shop.common.core.result.R;
import com.shop.product.dto.CreateProductRequest;
import com.shop.product.dto.ProductQueryDTO;
import com.shop.product.dto.ProductVO;
import com.shop.product.entity.Product;
import com.shop.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品控制器
 * <p>
 * 提供商品的分页查询、详情查看、创建、编辑、删除、上下架接口。
 * 所有接口统一返回R对象。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    /** 商品服务 */
    private final ProductService productService;

    /**
     * 分页查询商品列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    public R<PageResult<ProductVO>> pageProducts(ProductQueryDTO queryDTO) {
        return R.ok(productService.pageProducts(queryDTO));
    }

    /**
     * 查询商品详情
     *
     * @param id 商品ID
     * @return 商品详情（包含SKU列表）
     */
    @GetMapping("/{id}")
    public R<ProductVO> getProductDetail(@PathVariable Long id) {
        return R.ok(productService.getProductDetail(id));
    }

    /**
     * 创建商品
     *
     * @param request 创建请求（包含商品信息和SKU列表）
     * @return 操作结果
     */
    @PostMapping
    public R<Void> createProduct(@Validated @RequestBody CreateProductRequest request) {
        productService.createProduct(request.getProduct(), request.getSkuList());
        return R.ok();
    }

    /**
     * 更新商品
     *
     * @param id      商品ID
     * @param request 更新请求（包含商品信息和SKU列表）
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public R<Void> updateProduct(@PathVariable Long id,
                                 @Validated @RequestBody CreateProductRequest request) {
        productService.updateProduct(id, request.getProduct(), request.getSkuList());
        return R.ok();
    }

    /**
     * 删除商品
     *
     * @param id 商品ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public R<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return R.ok();
    }

    /**
     * 更新商品状态（上架/下架）
     *
     * @param id     商品ID
     * @param status 目标状态
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        productService.updateStatus(id, status);
        return R.ok();
    }

    /**
     * 批量删除商品
     *
     * @param ids 商品ID列表
     * @return 操作结果
     */
    @PostMapping("/batch/delete")
    public R<Void> batchDelete(@RequestBody List<Long> ids) {
        productService.batchDelete(ids);
        return R.ok();
    }

    /**
     * 更新商品库存
     * <p>
     * 供卡密服务通过Feign远程调用，通过增量方式更新商品总库存。
     * </p>
     *
     * @param productId 商品ID
     * @param delta     库存变化量
     * @return 操作结果
     */
    @PutMapping("/stock")
    public R<Void> updateStock(@RequestParam Long productId, @RequestParam Integer delta) {
        productService.updateStock(productId, delta);
        return R.ok();
    }
}
