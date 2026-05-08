package com.shop.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.auth.entity.MerchantUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商家用户Mapper
 *
 * @author shop
 * @since 1.0.0
 */
@Mapper
public interface MerchantUserMapper extends BaseMapper<MerchantUser> {
}
