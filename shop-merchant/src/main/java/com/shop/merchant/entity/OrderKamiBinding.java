package com.shop.merchant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单卡密绑定实体
 * <p>
 * 对应数据库表 t_order_kami_binding，
 * 存储支付成功后分配给订单的卡密明文（AES加密）。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Data
@TableName("t_order_kami_binding")
public class OrderKamiBinding implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租户ID */
    private Long tenantId;

    /** 订单号 */
    private String orderNo;

    /** 卡密条目ID */
    private Long kamiItemId;

    /** 卡密明文（AES加密存储） */
    private String kamiContentEncrypted;

    /** 交付时间 */
    private LocalDateTime deliveredAt;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
