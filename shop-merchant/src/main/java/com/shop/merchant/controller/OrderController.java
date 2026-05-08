package com.shop.merchant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.common.core.result.PageResult;
import com.shop.common.core.result.R;
import com.shop.merchant.entity.Order;
import com.shop.merchant.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 * <p>提供订单分页查询、详情查看接口</p>
 *
 * @author shop
 * @since 1.0.0
 */
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    /** 订单Mapper */
    private final OrderMapper orderMapper;

    /**
     * 分页查询订单列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param orderNo  订单号(可选)
     * @param status   状态(可选)
     * @return 分页结果
     */
    @GetMapping("/page")
    public R<PageResult<Order>> pageOrders(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        // 订单号模糊搜索
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.like(Order::getOrderNo, orderNo);
        }
        // 状态筛选
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        // 按创建时间倒序
        wrapper.orderByDesc(Order::getCreatedAt);

        Page<Order> page = orderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return R.ok(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    /**
     * 查询订单详情
     *
     * @param id 订单ID
     * @return 订单详情
     */
    @GetMapping("/{id}")
    public R<Order> getOrder(@PathVariable Long id) {
        return R.ok(orderMapper.selectById(id));
    }
}
