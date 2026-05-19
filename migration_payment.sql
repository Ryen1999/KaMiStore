-- =============================================
-- 交易支付功能数据库迁移脚本
-- 功能：支付宝支付 + 卡密锁定 + T+1结算
-- 日期：2026-05-13
-- =============================================

-- 1. 订单表新增结算字段
ALTER TABLE t_order
    ADD COLUMN settle_status TINYINT(2) DEFAULT 0 COMMENT '结算状态：0待结算, 1已结算, 2结算失败' AFTER deliver_time,
    ADD COLUMN settle_time DATETIME NULL COMMENT '结算时间' AFTER settle_status,
    ADD COLUMN settle_trade_no VARCHAR(64) NULL COMMENT '结算转账流水号' AFTER settle_time,
    ADD COLUMN settle_fail_reason VARCHAR(255) NULL COMMENT '结算失败原因' AFTER settle_trade_no,
    ADD INDEX idx_settle_scan (status, settle_status, pay_time);

-- 2. 结算配置新增平台抽成比例
ALTER TABLE t_settlement_config
    ADD COLUMN platform_commission_rate DECIMAL(6,4) DEFAULT 0.1000 COMMENT '平台抽成比例(0~1)' AFTER collection_type;

-- 3. 新增订单-卡密绑定表
CREATE TABLE IF NOT EXISTS t_order_kami_binding (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    order_no VARCHAR(64) NOT NULL COMMENT '订单号',
    kami_item_id BIGINT NOT NULL COMMENT '卡密条目ID',
    kami_content_encrypted TEXT NOT NULL COMMENT '卡密明文（AES加密存储）',
    delivered_at DATETIME NOT NULL COMMENT '交付时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_order (order_no),
    INDEX idx_kami (kami_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单卡密绑定表';
