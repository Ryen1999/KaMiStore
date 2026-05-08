package com.shop.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 商家用户实体
 *
 * @author shop
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_merchant_user")
public class MerchantUser extends BaseEntity {

    /** 租户ID（历史字段，默认0） */
    private Long tenantId = 0L;

    /** 用户名 */
    private String username;

    /** 密码(BCrypt加密) */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 是否主账号：1主账号 0子账号 */
    private Integer isOwner;

    /** 商家内部角色ID */
    private Long roleId;

    /** 状态：0禁用 1正常 */
    private Integer status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginAt;
}
