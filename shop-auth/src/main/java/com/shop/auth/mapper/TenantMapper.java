package com.shop.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.auth.entity.Tenant;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户Mapper
 *
 * @author shop
 * @since 1.0.0
 */
@Mapper
public interface TenantMapper extends BaseMapper<Tenant> {
}
