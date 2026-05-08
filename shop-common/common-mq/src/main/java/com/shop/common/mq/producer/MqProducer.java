package com.shop.common.mq.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * RocketMQ消息发送工具类
 * <p>
 * 封装RocketMQTemplate常用发送操作，统一消息序列化为JSON。
 * 提供同步发送、异步发送、延迟发送等能力。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MqProducer {

    /** RocketMQ发送模板 */
    private final RocketMQTemplate rocketMQTemplate;

    /** JSON序列化 */
    private final ObjectMapper objectMapper;

    /**
     * 同步发送消息
     *
     * @param topic   消息Topic
     * @param payload 消息体对象（自动序列化为JSON）
     */
    public void syncSend(String topic, Object payload) {
        String json = toJson(payload);
        Message<String> message = MessageBuilder.withPayload(json).build();
        rocketMQTemplate.syncSend(topic, message);
        log.info("MQ同步发送成功, topic={}, payload={}", topic, json);
    }

    /**
     * 发送延迟消息
     * <p>
     * RocketMQ延迟等级：
     * 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
     * 对应delayLevel: 1  2  3   4   5  6  7  8  9  10 11 12 13 14  15  16  17 18
     * </p>
     *
     * @param topic      消息Topic
     * @param payload    消息体对象
     * @param delayLevel 延迟等级（如16=30分钟，用于订单超时关闭）
     */
    public void delaySend(String topic, Object payload, int delayLevel) {
        String json = toJson(payload);
        Message<String> message = MessageBuilder.withPayload(json).build();
        rocketMQTemplate.syncSend(topic, message, 3000, delayLevel);
        log.info("MQ延迟发送成功, topic={}, delayLevel={}, payload={}", topic, delayLevel, json);
    }

    /**
     * 对象序列化为JSON字符串
     *
     * @param obj 待序列化对象
     * @return JSON字符串
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("消息序列化失败", e);
        }
    }
}
