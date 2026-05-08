package com.shop.merchant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 支付方式实体
 * <p>
 * 对应数据库表 t_payment_method，存储支付渠道配置信息。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_payment_method")
public class PaymentMethod extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 租户ID */
    private Long tenantId;

    /** 支付方式代码（如：alipay_scan, wechat_pay） */
    private String methodCode;

    /** 支付方式名称 */
    private String methodName;

    /** 图标名称（Element Plus图标） */
    private String icon;

    /** 图标背景色 */
    private String iconBg;

    /** 图标颜色 */
    private String iconColor;

    /** 描述信息 */
    private String description;

    /** 费率 */
    private String feeRate;

    /** 每日限额（万元） */
    private Integer dailyLimit;

    /** 商户号 */
    private String merchantId;

    /** API密钥 */
    private String apiKey;

    /** 私钥 */
    private String privateKey;

    /** 是否启用 */
    private Boolean enabled;
}