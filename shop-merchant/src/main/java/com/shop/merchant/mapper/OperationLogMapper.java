package com.shop.merchant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.merchant.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper
 *
 * @author shop
 * @since 1.0.0
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
