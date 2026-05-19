package com.shop.merchant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_payment_record")
public class PaymentRecord extends BaseEntity {

    private Long tenantId;

    private String orderNo;

    private String channel;

    private String channelTradeNo;

    private BigDecimal payAmount;

    private Integer payStatus;

    private Boolean verified;

    private String notifyPayload;

    private LocalDateTime notifyTime;
}
