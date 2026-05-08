package com.shop.merchant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站内消息实体
 *
 * @author shop
 * @since 1.0.0
 */
@Data
@TableName("t_notify_message")
public class NotifyMessage {

    /** 主键 */
    private Long id;

    /** 租户ID */
    private Long tenantId;

    /** 接收者ID */
    private Long receiverId;

    /** 接收者类型 1消费者 2商家 */
    private Integer receiverType;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 消息类型 1订单通知 2系统通知 3营销通知 */
    private Integer msgType;

    /** 是否已读 0未读 1已读 */
    private Integer isRead;

    /** 阅读时间 */
    private LocalDateTime readAt;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
