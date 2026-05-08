package com.shop.common.mq.constant;

/**
 * RocketMQ Topic/Tag 常量定义
 * <p>
 * 统一管理所有消息队列的Topic和Tag，避免硬编码。
 * Topic命名规则：TOPIC_模块_动作
 * Tag命名规则：TAG_具体事件
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
public class MqConstant {

    private MqConstant() {
    }

    // ==================== Topic定义 ====================

    /** 支付成功通知 - 由shop-pay发送，shop-order消费 */
    public static final String TOPIC_ORDER_PAID = "TOPIC_ORDER_PAID";

    /** 触发卡密自动发货 - 由shop-order发送，shop-kami消费 */
    public static final String TOPIC_KAMI_DELIVER = "TOPIC_KAMI_DELIVER";

    /** 订单完成通知 - 由shop-order发送，shop-finance/shop-statistics消费 */
    public static final String TOPIC_ORDER_COMPLETE = "TOPIC_ORDER_COMPLETE";

    /** 订单取消 - 由shop-order发送，shop-kami/shop-marketing消费 */
    public static final String TOPIC_ORDER_CANCEL = "TOPIC_ORDER_CANCEL";

    /** 订单超时关闭 - 由shop-order发送，shop-kami消费（释放预锁卡密） */
    public static final String TOPIC_ORDER_TIMEOUT = "TOPIC_ORDER_TIMEOUT";

    /** 卡密库存预警 - 由shop-kami发送，shop-message消费 */
    public static final String TOPIC_KAMI_STOCK_WARN = "TOPIC_KAMI_STOCK_WARN";

    /** 卡密发货成功通知消费者 - 由shop-kami发送，shop-message消费 */
    public static final String TOPIC_KAMI_DELIVERED = "TOPIC_KAMI_DELIVERED";

    /** 退款成功 - 由shop-pay发送，shop-order/shop-kami/shop-finance消费 */
    public static final String TOPIC_REFUND_SUCCESS = "TOPIC_REFUND_SUCCESS";

    /** 域名绑定变更 - 由shop-domain发送，shop-gateway消费 */
    public static final String TOPIC_DOMAIN_BIND_UPDATE = "TOPIC_DOMAIN_BIND_UPDATE";

    // ==================== 消费者组定义 ====================

    /** 订单服务消费者组 */
    public static final String GROUP_ORDER = "GID_ORDER";

    /** 卡密服务消费者组 */
    public static final String GROUP_KAMI = "GID_KAMI";

    /** 财务服务消费者组 */
    public static final String GROUP_FINANCE = "GID_FINANCE";

    /** 消息服务消费者组 */
    public static final String GROUP_MESSAGE = "GID_MESSAGE";

    /** 统计服务消费者组 */
    public static final String GROUP_STATISTICS = "GID_STATISTICS";

    /** 网关服务消费者组 */
    public static final String GROUP_GATEWAY = "GID_GATEWAY";
}
