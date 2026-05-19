package com.shop.merchant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_profit_sharing_record")
public class ProfitSharingRecord extends BaseEntity {

    private Long tenantId;

    private String orderNo;

    private BigDecimal orderAmount;

    private BigDecimal commissionRate;

    private BigDecimal platformAmount;

    private BigDecimal merchantAmount;

    private String channel;

    private String channelSharingNo;

    private Integer status;

    private LocalDateTime sharedAt;

    private String failReason;
}
