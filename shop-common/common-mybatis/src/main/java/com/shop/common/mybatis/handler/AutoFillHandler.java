package com.shop.common.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus自动填充处理器
 * <p>
 * 自动填充created_at和updated_at字段，无需在业务代码中手动设置。
 * 配合BaseEntity中的@TableField(fill = FieldFill.INSERT)注解使用。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Slf4j
@Component
public class AutoFillHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充
     * <p>填充created_at和updated_at为当前时间</p>
     *
     * @param metaObject 元数据对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime::now, LocalDateTime.class);
    }

    /**
     * 更新时自动填充
     * <p>填充updated_at为当前时间</p>
     *
     * @param metaObject 元数据对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime::now, LocalDateTime.class);
    }
}
