package com.shop.merchant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.merchant.entity.OrderKamiBinding;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单卡密绑定Mapper
 * <p>
 * 提供t_order_kami_binding表的数据库访问能力。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Mapper
public interface OrderKamiBindingMapper extends BaseMapper<OrderKamiBinding> {
}
