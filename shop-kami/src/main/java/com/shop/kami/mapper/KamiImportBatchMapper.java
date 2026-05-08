package com.shop.kami.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.kami.entity.KamiImportBatch;
import org.apache.ibatis.annotations.Mapper;

/**
 * 卡密导入批次Mapper接口
 * <p>
 * 提供卡密导入批次表 t_kami_import_batch 的数据库访问能力。
 * 继承MyBatis-Plus的BaseMapper，自动获得CRUD方法。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Mapper
public interface KamiImportBatchMapper extends BaseMapper<KamiImportBatch> {
}
