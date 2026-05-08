package com.shop.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.auth.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

/**
 * 平台管理员Mapper
 *
 * @author shop
 * @since 1.0.0
 */
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
}
