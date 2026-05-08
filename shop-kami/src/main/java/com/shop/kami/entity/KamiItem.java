package com.shop.kami.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 卡密条目实体
 * <p>
 * 对应数据库表 t_kami_item，存储单条卡密信息。
 * 卡密内容使用AES加密存储，通过SHA256哈希值进行去重校验。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_kami_item")
public class KamiItem extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 租户ID */
    private Long tenantId;

    /** 所属商品ID */
    private Long productId;

    /** 所属导入批次ID */
    private Long batchId;

    /** 卡密内容（AES加密后的密文） */
    private String kamiContent;

    /** 卡密哈希值（SHA256，用于去重） */
    private String kamiHash;

    /**
     * 卡密状态
     * <ul>
     *   <li>0 - 未售（可用）</li>
     *   <li>1 - 锁定（下单锁定中）</li>
     *   <li>2 - 已售（已发货）</li>
     *   <li>3 - 作废</li>
     * </ul>
     */
    private Integer status;

    /** 锁定令牌（用于分布式锁定场景） */
    private String lockToken;

    /** 锁定过期时间 */
    private LocalDateTime lockExpireAt;

    /** 关联订单ID */
    private Long orderId;

    /** 关联订单项ID */
    private Long orderItemId;

    /** 售出时间 */
    private LocalDateTime soldAt;

    /** 作废时间 */
    private LocalDateTime voidAt;

    /** 作废原因 */
    private String voidReason;
}
