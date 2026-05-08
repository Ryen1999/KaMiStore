package com.shop.kami.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.common.core.context.TenantContext;
import com.shop.common.core.exception.BizException;
import com.shop.common.core.result.PageResult;
import com.shop.kami.dto.KamiImportDTO;
import com.shop.kami.dto.KamiItemQueryDTO;
import com.shop.kami.entity.KamiImportBatch;
import com.shop.kami.entity.KamiItem;
import com.shop.kami.feign.ProductFeignClient;
import com.shop.kami.mapper.KamiImportBatchMapper;
import com.shop.kami.mapper.KamiItemMapper;
import com.shop.kami.util.KamiEncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 卡密条目服务
 * <p>
 * 提供卡密的分页查询、批量导入、作废和明文查看功能。
 * 卡密直接关联商品，库存同步到商品。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KamiItemService {

    /** 卡密条目Mapper */
    private final KamiItemMapper kamiItemMapper;

    /** 卡密导入批次Mapper */
    private final KamiImportBatchMapper kamiImportBatchMapper;

    /** 商品服务Feign客户端 */
    private final ProductFeignClient productFeignClient;

    /** Redis模板（用于分布式锁） */
    private final StringRedisTemplate stringRedisTemplate;

    /** 分布式锁key前缀 */
    private static final String IMPORT_LOCK_PREFIX = "kami:import:lock:";

    /** 分布式锁超时时间（秒） */
    private static final long LOCK_TIMEOUT = 300L;

    /**
     * 分页查询卡密列表
     *
     * @param queryDTO 查询条件（商品ID、状态、分页参数）
     * @return 分页结果（不含卡密明文）
     */
    public PageResult<KamiItem> pageItems(KamiItemQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<KamiItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KamiItem::getTenantId, TenantContext.getTenantId());
        // 按商品ID过滤（可选）
        wrapper.eq(queryDTO.getProductId() != null, KamiItem::getProductId, queryDTO.getProductId());
        // 按状态过滤（可选）
        wrapper.eq(queryDTO.getStatus() != null, KamiItem::getStatus, queryDTO.getStatus());
        // 不查询卡密明文内容（安全考虑）
        wrapper.select(KamiItem::getId, KamiItem::getProductId, KamiItem::getBatchId,
                KamiItem::getKamiHash, KamiItem::getStatus, KamiItem::getLockToken,
                KamiItem::getLockExpireAt, KamiItem::getOrderId, KamiItem::getOrderItemId,
                KamiItem::getSoldAt, KamiItem::getVoidAt, KamiItem::getVoidReason,
                KamiItem::getCreatedAt, KamiItem::getUpdatedAt);
        // 按创建时间倒序
        wrapper.orderByDesc(KamiItem::getCreatedAt);

        // 执行分页查询
        Page<KamiItem> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<KamiItem> result = kamiItemMapper.selectPage(page, wrapper);

        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    /**
     * 批量导入卡密
     * <p>
     * 核心导入逻辑：
     * 1. 使用Redis分布式锁防止同一商品并发导入
     * 2. 创建导入批次记录
     * 3. 对每条卡密进行SHA256哈希去重检查
     * 4. AES加密存储卡密内容
     * 5. 直接更新商品库存数量
     * 6. 更新批次状态
     * </p>
     *
     * @param dto 导入参数（商品ID、卡密内容列表）
     * @return 导入批次记录（含成功数、失败数、失败明细）
     */
    @Transactional(rollbackFor = Exception.class)
    public KamiImportBatch importKami(KamiImportDTO dto) {
        Long productId = dto.getProductId();
        String lockKey = IMPORT_LOCK_PREFIX + productId;

        // 尝试获取分布式锁，防止并发导入
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", LOCK_TIMEOUT, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            throw new BizException("该商品正在导入中，请稍后重试");
        }

        try {
            return doImport(dto);
        } finally {
            // 释放分布式锁
            stringRedisTemplate.delete(lockKey);
        }
    }

    /**
     * 执行卡密导入核心逻辑
     *
     * @param dto 导入参数
     * @return 导入批次记录
     */
    private KamiImportBatch doImport(KamiImportDTO dto) {
        Long productId = dto.getProductId();
        Long tenantId = TenantContext.getTenantId();

        // 1. 创建导入批次记录
        KamiImportBatch batch = new KamiImportBatch();
        batch.setTenantId(tenantId);
        batch.setProductId(productId);
        batch.setFileName("API导入");
        batch.setFileType("api");
        batch.setTotalCount(dto.getKamiList().size());
        batch.setSuccessCount(0);
        batch.setFailCount(0);
        batch.setStatus(0);
        batch.setStartedAt(LocalDateTime.now());
        kamiImportBatchMapper.insert(batch);

        // 2. 逐条处理卡密
        int successCount = 0;
        int failCount = 0;
        List<Map<String, String>> failDetails = new ArrayList<>();

        for (String kamiContent : dto.getKamiList()) {
            try {
                // SHA256哈希用于去重
                String kamiHash = KamiEncryptUtil.hash(kamiContent);

                // 检查是否已存在（通过哈希去重，按商品ID）
                LambdaQueryWrapper<KamiItem> dupWrapper = new LambdaQueryWrapper<>();
                dupWrapper.eq(KamiItem::getProductId, productId);
                dupWrapper.eq(KamiItem::getKamiHash, kamiHash);
                Long dupCount = kamiItemMapper.selectCount(dupWrapper);
                if (dupCount > 0) {
                    // 记录重复失败
                    failCount++;
                    Map<String, String> detail = new HashMap<>(2);
                    detail.put("kami", kamiContent.substring(0, Math.min(kamiContent.length(), 8)) + "***");
                    detail.put("reason", "卡密重复");
                    failDetails.add(detail);
                    continue;
                }

                // AES加密存储
                String encrypted = KamiEncryptUtil.encrypt(kamiContent);

                // 创建卡密条目
                KamiItem item = new KamiItem();
                item.setTenantId(tenantId);
                item.setProductId(productId);
                item.setBatchId(batch.getId());
                item.setKamiContent(encrypted);
                item.setKamiHash(kamiHash);
                item.setStatus(0);
                kamiItemMapper.insert(item);

                successCount++;
            } catch (Exception e) {
                // 单条导入失败不影响其他条目
                failCount++;
                Map<String, String> detail = new HashMap<>(2);
                detail.put("kami", kamiContent.substring(0, Math.min(kamiContent.length(), 8)) + "***");
                detail.put("reason", e.getMessage());
                failDetails.add(detail);
                log.warn("卡密导入单条失败, productId={}, error={}", productId, e.getMessage());
            }
        }

        // 3. 更新批次状态
        batch.setSuccessCount(successCount);
        batch.setFailCount(failCount);
        batch.setFinishedAt(LocalDateTime.now());
        if (CollUtil.isNotEmpty(failDetails)) {
            batch.setFailDetail(JSONUtil.toJsonStr(failDetails));
        }
        // 判断批次状态
        if (failCount == 0) {
            batch.setStatus(1);
        } else if (successCount == 0) {
            batch.setStatus(3);
        } else {
            batch.setStatus(2);
        }
        kamiImportBatchMapper.updateById(batch);

        // 4. 同步商品库存
        if (successCount > 0) {
            try {
                productFeignClient.updateStock(productId, successCount);
            } catch (Exception e) {
                log.warn("同步商品库存失败, productId={}, error={}", productId, e.getMessage());
            }
        }

        log.info("卡密导入完成, productId={}, batchId={}, success={}, fail={}",
                productId, batch.getId(), successCount, failCount);
        return batch;
    }

    /**
     * 作废卡密
     * <p>
     * 将卡密状态更新为作废（status=3）。
     * </p>
     *
     * @param id     卡密条目ID
     * @param reason 作废原因
     */
    @Transactional(rollbackFor = Exception.class)
    public void voidKami(Long id, String reason) {
        // 查询卡密
        KamiItem item = kamiItemMapper.selectById(id);
        if (item == null) {
            throw new BizException("卡密不存在");
        }
        // 只有未售状态的卡密可以作废
        if (item.getStatus() != 0) {
            throw new BizException("只有未售状态的卡密可以作废");
        }

        // 更新卡密状态
        item.setStatus(3);
        item.setVoidAt(LocalDateTime.now());
        item.setVoidReason(reason);
        kamiItemMapper.updateById(item);

        log.info("卡密作废成功, kamiId={}, reason={}", id, reason);
    }

    /**
     * 查看卡密明文
     * <p>
     * 对AES加密的卡密内容进行解密，仅用于管理后台查看。
     * </p>
     *
     * @param id 卡密条目ID
     * @return 解密后的卡密明文内容
     */
    public String getKamiContent(Long id) {
        KamiItem item = kamiItemMapper.selectById(id);
        if (item == null) {
            throw new BizException("卡密不存在");
        }
        // AES解密返回明文
        return KamiEncryptUtil.decrypt(item.getKamiContent());
    }
}