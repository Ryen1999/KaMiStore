package com.shop.merchant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.merchant.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单Mapper
 *
 * @author shop
 * @since 1.0.0
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
