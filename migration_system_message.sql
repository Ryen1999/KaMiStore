-- =============================================
-- 站内消息功能数据库迁移脚本
-- 日期：2026-05-14
-- =============================================

CREATE TABLE IF NOT EXISTS t_notify_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    receiver_id BIGINT NULL COMMENT '接收者ID',
    receiver_type TINYINT(2) DEFAULT 2 COMMENT '接收者类型：1消费者, 2商家',
    title VARCHAR(255) NOT NULL COMMENT '标题',
    content VARCHAR(1000) NULL COMMENT '内容',
    msg_type TINYINT(2) DEFAULT 2 COMMENT '消息类型：1订单通知, 2系统通知, 3营销通知',
    is_read TINYINT(1) DEFAULT 0 COMMENT '是否已读：0未读, 1已读',
    read_at DATETIME NULL COMMENT '阅读时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_tenant_read_time (tenant_id, is_read, created_at),
    INDEX idx_receiver (receiver_id, receiver_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息表';
