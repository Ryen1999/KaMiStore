package com.shop.merchant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 结算配置实体
 * <p>
 * 对应数据库表 t_settlement_config，存储商户的结算配置信息。
 * 每个租户拥有一条结算配置记录。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_settlement_config")
public class SettlementConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 租户ID */
    private Long tenantId;

    /** 收款类型：system-系统默认, manual-手工提现, auto-自动提现 */
    private String collectionType;

    /** 支付宝账号 */
    private String alipayAccount;

    /** 收款人姓名 */
    private String payeeName;

    /** 收款二维码URL */
    private String qrcodeUrl;
}
