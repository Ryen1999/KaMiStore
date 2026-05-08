package com.shop.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.common.core.context.TenantContext;
import com.shop.common.core.exception.BizException;
import com.shop.merchant.entity.SettlementConfig;
import com.shop.merchant.mapper.SettlementConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 结算配置服务
 * <p>
 * 提供结算配置的管理功能：获取配置、更新配置。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementConfigService {

    /** 结算配置Mapper */
    private final SettlementConfigMapper settlementConfigMapper;

    /**
     * 获取结算配置
     *
     * @return 结算配置
     */
    public SettlementConfig getConfig() {
        LambdaQueryWrapper<SettlementConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SettlementConfig::getTenantId, TenantContext.getTenantId());
        SettlementConfig config = settlementConfigMapper.selectOne(wrapper);
        
        // 如果不存在，创建默认配置
        if (config == null) {
            config = new SettlementConfig();
            config.setTenantId(TenantContext.getTenantId());
            config.setCollectionType("system");
            settlementConfigMapper.insert(config);
            log.info("创建默认结算配置, tenantId={}", TenantContext.getTenantId());
        }
        
        return config;
    }

    /**
     * 更新结算配置
     *
     * @param collectionType 收款类型
     * @param alipayAccount  支付宝账号
     * @param payeeName      收款人姓名
     * @param qrcodeUrl      收款二维码URL
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(String collectionType, String alipayAccount, String payeeName, String qrcodeUrl) {
        LambdaQueryWrapper<SettlementConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SettlementConfig::getTenantId, TenantContext.getTenantId());
        SettlementConfig config = settlementConfigMapper.selectOne(wrapper);
        
        if (config == null) {
            // 不存在则创建
            config = new SettlementConfig();
            config.setTenantId(TenantContext.getTenantId());
        }
        
        config.setCollectionType(collectionType);
        config.setAlipayAccount(alipayAccount);
        config.setPayeeName(payeeName);
        config.setQrcodeUrl(qrcodeUrl);
        
        if (config.getId() == null) {
            settlementConfigMapper.insert(config);
            log.info("创建结算配置成功, tenantId={}", TenantContext.getTenantId());
        } else {
            settlementConfigMapper.updateById(config);
            log.info("更新结算配置成功, tenantId={}", TenantContext.getTenantId());
        }
    }
}
