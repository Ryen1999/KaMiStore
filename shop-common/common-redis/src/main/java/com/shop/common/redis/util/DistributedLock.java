package com.shop.common.redis.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁工具类（基于Redisson）
 * <p>
 * 封装Redisson的分布式锁操作，提供简化的加锁/解锁接口。
 * 主要用于：卡密发货防重复、库存扣减防超卖等并发场景。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLock {

    /** Redisson客户端 */
    private final RedissonClient redissonClient;

    /**
     * 尝试获取分布式锁
     *
     * @param lockKey   锁的key
     * @param waitTime  等待获取锁的最大时间
     * @param leaseTime 锁自动释放时间（防止死锁）
     * @param unit      时间单位
     * @return true-获取成功 false-获取失败（超时）
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断, key={}", lockKey, e);
            return false;
        }
    }

    /**
     * 释放分布式锁
     *
     * @param lockKey 锁的key
     */
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        // 只有当前线程持有锁时才释放，防止释放别人的锁
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
