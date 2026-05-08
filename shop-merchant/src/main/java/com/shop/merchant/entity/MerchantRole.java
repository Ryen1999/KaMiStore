package com.shop.merchant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商户角色实体
 * <p>
 * 对应数据库表 t_merchant_role，存储商户自定义的角色信息。
 * 每个租户可创建多个角色，用于商户内部的权限分配。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_merchant_role")
public class MerchantRole extends BaseEntity {

    /** 租户ID */
    private Long tenantId;

    /** 角色名称 */
    private String roleName;

    /** 角色编码（唯一标识） */
    private String roleCode;

    /** 权限列表（JSON格式） */
    private String permissions;
}
