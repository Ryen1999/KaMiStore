package com.shop.kami.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.shop.common.core.exception.BizException;
import com.shop.common.redis.util.DistributedLock;
import com.shop.kami.entity.KamiItem;
import com.shop.kami.mapper.KamiItemMapper;
import com.shop.kami.util.KamiEncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 卡密分配服务
 * <p>
 * 提供卡密锁定（预锁）、确认分配（锁定→已售）、
 * 过期释放的核心业务逻辑。
 * 使用分布式锁保证并发安全，FIFO选出卡密。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KamiAllocateService {

    /** 卡密条目Mapper */
    private final KamiItemMapper kamiItemMapper;

    /** 分布式锁工具 */
    private final DistributedLock distributedLock;

    /**
     * 锁定N条卡密
     * <p>
     * 按FIFO取出status=0的卡密，原子更新为status=1（锁定），
     * 设置lockToken=orderNo和lockExpireAt超时时间。
     * 使用Redisson分布式锁防并发。
     * </p>
     *
     * @param tenantId         租户ID
     * @param productId        商品ID
     * @param skuId            SKU ID
     * @param quantity         锁定数量
     * @param orderNo          订单号
     * @param lockTtlSeconds   锁定时长（秒）
     * @return 锁定的卡密ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Long> lock(Long tenantId, Long productId, Long skuId, int quantity,
                           String orderNo, int lockTtlSeconds) {
        String lockKey = "kami:alloc:" + productId + ":" + skuId;

        boolean acquired = false;
        try {
            acquired = distributedLock.tryLock(lockKey, 5, 30, TimeUnit.SECONDS);
            if (!acquired) {
                throw new BizException("卡密锁定繁忙，请稍后重试");
            }

            // 查询可用的卡密，按创建时间升序（FIFO）
            List<KamiItem> availableList = kamiItemMapper.selectList(
                    new LambdaQueryWrapper<KamiItem>()
                            .eq(KamiItem::getTenantId, tenantId)
                            .eq(KamiItem::getProductId, productId)
                            .eq(KamiItem::getStatus, 0)
                            .orderByAsc(KamiItem::getCreatedAt)
                            .last("LIMIT " + quantity)
            );

            if (availableList.size() < quantity) {
                throw new BizException("卡密库存不足，当前可用：" + availableList.size());
            }

            LocalDateTime expireAt = LocalDateTime.now().plusSeconds(lockTtlSeconds);
            List<Long> lockedIds = new ArrayList<>();

            for (KamiItem item : availableList) {
                LambdaUpdateWrapper<KamiItem> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(KamiItem::getId, item.getId())
                        .eq(KamiItem::getStatus, 0)
                        .set(KamiItem::getStatus, 1)
                        .set(KamiItem::getLockToken, orderNo)
                        .set(KamiItem::getLockExpireAt, expireAt);

                int rows = kamiItemMapper.update(null, updateWrapper);
                if (rows > 0) {
                    lockedIds.add(item.getId());
                }
            }

            if (lockedIds.size() < quantity) {
                log.warn("卡密锁定部分失败 orderNo={}, expected={}, actual={}", orderNo, quantity, lockedIds.size());
                // 回滚已锁定的卡密
                rollbackLockedItems(lockedIds);
                throw new BizException("卡密锁定失败，请重试");
            }

            log.info("卡密锁定成功 orderNo={}, count={}, expireAt={}", orderNo, lockedIds.size(), expireAt);
            return lockedIds;
        } finally {
            if (acquired) {
                distributedLock.unlock(lockKey);
            }
        }
    }

    /**
     * 确认卡密分配（锁定→已售）
     * <p>
     * 根据orderNo查找锁定的卡密（status=1且lockToken=orderNo），
     * 将状态从1(锁定)改为2(已售)，解密卡密内容后返回明文列表。
     * </p>
     *
     * @param tenantId 租户ID
     * @param orderNo  订单号
     * @return 卡密分配结果（kamiItemId + 明文内容）
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> confirm(Long tenantId, String orderNo) {
        // 按orderNo查找所有锁定的卡密
        List<KamiItem> lockedItems = kamiItemMapper.selectList(
                new LambdaQueryWrapper<KamiItem>()
                        .eq(KamiItem::getTenantId, tenantId)
                        .eq(KamiItem::getStatus, 1)
                        .eq(KamiItem::getLockToken, orderNo)
        );

        if (lockedItems.isEmpty()) {
            throw new BizException("未找到锁定卡密，orderNo=" + orderNo + "，可能已过期释放");
        }

        List<Map<String, Object>> resultList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (KamiItem item : lockedItems) {
            // 原子更新：status:1→2，清除锁信息
            LambdaUpdateWrapper<KamiItem> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(KamiItem::getId, item.getId())
                    .eq(KamiItem::getStatus, 1)
                    .eq(KamiItem::getLockToken, orderNo)
                    .set(KamiItem::getStatus, 2)
                    .set(KamiItem::getSoldAt, now)
                    .set(KamiItem::getLockToken, null)
                    .set(KamiItem::getLockExpireAt, null);

            int rows = kamiItemMapper.update(null, updateWrapper);
            if (rows == 0) {
                log.error("卡密确认失败，更新行数为0 kamiItemId={}, orderNo={}", item.getId(), orderNo);
                throw new BizException("卡密确认失败 ID=" + item.getId());
            }

            String plainContent = KamiEncryptUtil.decrypt(item.getKamiContent());

            Map<String, Object> resultItem = new HashMap<>(2);
            resultItem.put("kamiItemId", item.getId());
            resultItem.put("plainContent", plainContent);
            resultList.add(resultItem);
        }

        log.info("卡密确认成功 orderNo={}, count={}", orderNo, resultList.size());
        return resultList;
    }

    /**
     * 释放过期卡密锁
     * <p>
     * 扫描所有status=1(锁定)且lockExpireAt已过期的卡密，
     * 将其恢复为status=0(未售)。
     * 由定时任务SettlementJob触发。
     * </p>
     *
     * @return 释放的卡密数量
     */
    public int releaseExpired() {
        LambdaUpdateWrapper<KamiItem> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(KamiItem::getStatus, 1)
                .lt(KamiItem::getLockExpireAt, LocalDateTime.now())
                .set(KamiItem::getStatus, 0)
                .set(KamiItem::getLockToken, null)
                .set(KamiItem::getLockExpireAt, null);

        int count = kamiItemMapper.update(null, updateWrapper);
        if (count > 0) {
            log.info("释放过期卡密锁 count={}", count);
        }
        return count;
    }

    /**
     * 回滚已锁定的卡密
     * <p>
     * 当批量锁定部分失败时，将已锁定成功的卡密恢复为未售状态。
     * </p>
     *
     * @param ids 已锁定的卡密ID列表
     */
    private void rollbackLockedItems(List<Long> ids) {
        for (Long id : ids) {
            LambdaUpdateWrapper<KamiItem> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(KamiItem::getId, id)
                    .eq(KamiItem::getStatus, 1)
                    .set(KamiItem::getStatus, 0)
                    .set(KamiItem::getLockToken, null)
                    .set(KamiItem::getLockExpireAt, null);
            kamiItemMapper.update(null, updateWrapper);
        }
    }
}
