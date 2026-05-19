package com.shop.merchant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_order")
public class Order extends BaseEntity {

    private String orderNo;

    private Long tenantId;

    private Long productId;

    private String productName;

    private Long skuId;

    private String skuName;

    private Integer quantity;

    private Long buyerId;

    private Integer orderType;

    private Integer contactType;

    private String contactValue;

    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    private BigDecimal payAmount;

    private BigDecimal platformCommissionAmount;

    private BigDecimal merchantSettleAmount;

    private Integer payMethod;

    private String payTradeNo;

    private LocalDateTime payTime;

    private Integer status;

    private LocalDateTime deliverTime;

    /** 结算状态：0待结算，1已结算，2结算失败 */
    private Integer settleStatus;

    /** 结算时间 */
    private LocalDateTime settleTime;

    /** 结算转账流水号 */
    private String settleTradeNo;

    /** 结算失败原因 */
    private String settleFailReason;
}
