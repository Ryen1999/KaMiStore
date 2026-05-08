package com.shop.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.product.entity.Product;

/**
 * 商品Mapper接口
 * <p>
 * 继承MyBatis-Plus的BaseMapper，提供商品表的基础CRUD操作。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
public interface ProductMapper extends BaseMapper<Product> {
}
