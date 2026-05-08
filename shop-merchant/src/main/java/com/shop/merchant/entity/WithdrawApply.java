package com.shop.merchant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 提现申请实体
 *
 * @author shop
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_withdraw_apply")
public class WithdrawApply extends BaseEntity {

    /** 租户ID */
    private Long tenantId;

    /** 提现编号 */
    private String withdrawNo;

    /** 提现金额 */
    private BigDecimal amount;

    /** 手续费 */
    private BigDecimal fee;

    /** 实际到账 */
    private BigDecimal actualAmount;

    /** 银行名称 */
    private String bankName;

    /** 银行账号 */
    private String bankAccount;

    /** 户名 */
    private String bankAccountName;

    /** 状态 0待审核 1审核通过 2已到账 3审核拒绝 */
    private Integer status;

    /** 拒绝原因 */
    private String rejectReason;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 打款时间 */
    private LocalDateTime payTime;
}
