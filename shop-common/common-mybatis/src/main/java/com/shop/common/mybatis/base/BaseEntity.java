package com.shop.common.mybatis.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类
 * <p>
 * 所有业务实体类继承此基类，统一主键策略和公共字段。
 * 主键使用Snowflake算法生成（ASSIGN_ID），自动填充创建时间和更新时间。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Data
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（Snowflake雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 创建时间（INSERT时自动填充）
     */
    @TableField(fill = FieldFill.INSERT, value = "created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间（INSERT和UPDATE时自动填充）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE, value = "updated_at")
    private LocalDateTime updatedAt;
}
