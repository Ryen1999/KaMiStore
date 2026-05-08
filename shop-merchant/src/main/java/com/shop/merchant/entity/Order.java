package com.shop.merchant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 *
 * @author shop
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_order")
public class Order extends BaseEntity {

    /** 订单编号 */
    private String orderNo;

    /** 买家ID */
    private Long buyerId;

    /** 订单类型 1实物 2卡密 */
    private Integer orderType;

    /** 联系方式类型 */
    private Integer contactType;

    /** 联系方式 */
    private String contactValue;

    /** 商品总额 */
    private BigDecimal totalAmount;

    /** 优惠金额 */
    private BigDecimal discountAmount;

    /** 实付金额 */
    private BigDecimal payAmount;

    /** 支付方式 1微信 2支付宝 */
    private Integer payMethod;

    /** 支付流水号 */
    private String payTradeNo;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 状态 0待付款 1已付款 2已发货 3已完成 4已取消 */
    private Integer status;

    /** 发货时间 */
    private LocalDateTime deliverTime;
}
