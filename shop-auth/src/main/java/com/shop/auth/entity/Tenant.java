package com.shop.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 租户/商家实体
 * <p>
 * 对应数据库表 t_tenant，存储商户入驻信息。
 * 注册时自动创建一条租户记录。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_tenant")
public class Tenant extends BaseEntity {

    /** 店铺名称 */
    private String tenantName;

    /** 店铺Logo */
    private String logo;

    /** 联系人 */
    private String contactName;

    /** 联系电话 */
    private String contactPhone;

    /** 邮箱 */
    private String email;

    /** 状态：0待审核 1正常 2禁用 3审核驳回 */
    private Integer status;

    /** 套餐类型：1基础版 2专业版 3企业版 */
    private Integer planType;

    /** 套餐到期时间 */
    private LocalDateTime planExpireAt;

    /** 平台服务费率(%) */
    private BigDecimal serviceFeeRate;
}
