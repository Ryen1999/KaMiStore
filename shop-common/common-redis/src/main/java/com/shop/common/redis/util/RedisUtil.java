package com.shop.common.redis.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis操作工具类
 * <p>
 * 封装RedisTemplate常用操作，简化业务层Redis调用代码。
 * 包含String类型的set/get/delete/expire等基本操作。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class RedisUtil {

    /** Redis操作模板 */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置缓存（带过期时间）
     *
     * @param key     缓存key
     * @param value   缓存值
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 设置缓存（不过期）
     *
     * @param key   缓存key
     * @param value 缓存值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 获取缓存值
     *
     * @param key 缓存key
     * @return 缓存值，不存在返回null
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存
     *
     * @param key 缓存key
     * @return true-删除成功 false-key不存在
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 判断key是否存在
     *
     * @param key 缓存key
     * @return true-存在 false-不存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置过期时间
     *
     * @param key     缓存key
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return true-设置成功
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 原子递增
     *
     * @param key   缓存key
     * @param delta 递增值
     * @return 递增后的值
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }
}
