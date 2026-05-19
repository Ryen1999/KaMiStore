package com.shop.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.common.core.context.TenantContext;
import com.shop.merchant.dto.StoreConfigDTO;
import com.shop.merchant.entity.StoreConfig;
import com.shop.merchant.mapper.StoreConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 店铺配置服务
 * <p>
 * 提供店铺配置的查询与更新功能。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class StoreConfigService {

    /** 店铺配置Mapper */
    private final StoreConfigMapper storeConfigMapper;

    /**
     * 获取店铺配置
     * <p>
     * 按当前租户ID查询配置，若不存在则自动创建默认配置并返回。
     * </p>
     *
     * @return 店铺配置实体
     */
    public StoreConfig getStoreConfig() {
        Long tenantId = TenantContext.getTenantId();
        // 按租户ID查询店铺配置
        StoreConfig config = storeConfigMapper.selectOne(
                new LambdaQueryWrapper<StoreConfig>()
                        .eq(tenantId != null, StoreConfig::getTenantId, tenantId)
                        .last(tenantId == null, "LIMIT 1")
        );
        // 若配置不存在，则创建默认配置
        if (config == null) {
            config = new StoreConfig();
            config.setTenantId(tenantId);
            config.setStoreName("我的店铺");
            config.setThemeColor("#1890ff");
            config.setShopCode(generateShopCode());
            storeConfigMapper.insert(config);
        }
        return config;
    }

    /**
     * 生成唯一的店铺标识码
     * <p>
     * 使用8位随机字符，确保唯一性
     * </p>
     *
     * @return 唯一的shopCode
     */
    private String generateShopCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        java.util.Random random = new java.util.Random();
        StringBuilder code;
        do {
            code = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                code.append(chars.charAt(random.nextInt(chars.length())));
            }
        } while (isShopCodeExists(code.toString()));
        return code.toString();
    }

    /**
     * 检查shopCode是否已存在
     *
     * @param shopCode 店铺标识码
     * @return 是否存在
     */
    private boolean isShopCodeExists(String shopCode) {
        return storeConfigMapper.selectCount(
                new LambdaQueryWrapper<StoreConfig>()
                        .eq(StoreConfig::getShopCode, shopCode)
        ) > 0;
    }

    /**
     * 获取店铺链接
     * <p>
     * 获取当前商户的店铺访问链接
     * </p>
     *
     * @return 店铺链接
     */
    public String getShopLink() {
        StoreConfig config = getStoreConfig();
        if (config.getShopCode() == null) {
            config.setShopCode(generateShopCode());
            storeConfigMapper.updateById(config);
        }
        return "/shop/" + config.getShopCode();
    }

    /**
     * 根据shopCode查询店铺配置
     *
     * @param shopCode 店铺标识码
     * @return 店铺配置，未找到返回null
     */
    public StoreConfig getByShopCode(String shopCode) {
        return storeConfigMapper.selectOne(
                new LambdaQueryWrapper<StoreConfig>()
                        .eq(StoreConfig::getShopCode, shopCode)
                        .eq(StoreConfig::getStoreEnabled, 1)
        );
    }

    public StoreConfig getByTenantId(Long tenantId) {
        if (tenantId == null) {
            return null;
        }
        return storeConfigMapper.selectOne(
                new LambdaQueryWrapper<StoreConfig>()
                        .eq(StoreConfig::getTenantId, tenantId)
                        .last("LIMIT 1")
        );
    }

    /**
     * 更新店铺配置
     *
     * @param dto 店铺配置更新参数
     */
    public void updateStoreConfig(StoreConfigDTO dto) {
        StoreConfig config = getStoreConfig();
        
        // 基础信息
        config.setStoreName(dto.getStoreName());
        config.setStoreLogo(dto.getStoreLogo());
        config.setStoreDesc(dto.getStoreDesc());
        config.setStoreNotice(dto.getStoreNotice());
        
        // 店铺状态与样式
        if (dto.getStoreEnabled() != null) {
            config.setStoreEnabled(dto.getStoreEnabled() ? 1 : 0);
        } else {
            config.setStoreEnabled(1); // 默认启用
        }
        config.setThemeColor(dto.getThemeColor());
        config.setPcStyle(dto.getPcStyle());
        config.setMobileStyle(dto.getMobileStyle());
        config.setBgMusicUrl(dto.getBgMusicUrl());
        
        // 客服与社群
        config.setMerchantQq(dto.getMerchantQq());
        config.setGroupLink(dto.getGroupLink());
        
        // 公告设置
        if (dto.getAutoPopNotice() != null) {
            config.setAutoPopNotice(dto.getAutoPopNotice() ? 1 : 0);
        } else {
            config.setAutoPopNotice(0); // 默认关闭
        }
        
        // SEO配置
        config.setSeoTitle(dto.getSeoTitle());
        config.setSeoKeywords(dto.getSeoKeywords());
        config.setSeoDescription(dto.getSeoDescription());
        
        // 装修与模板
        config.setTemplateId(dto.getTemplateId() != null ? Long.parseLong(dto.getTemplateId()) : null);
        config.setDecorationJson(dto.getDecorationJson());
        config.setFooterHtml(dto.getFooterHtml());
        
        // 店铺标识码：如果前端传入了shopCode则使用，否则保持原有值；如果原值为null则生成新的
        if (dto.getShopCode() != null && !dto.getShopCode().isEmpty()) {
            // 验证shopCode是否已被其他租户使用
            StoreConfig existingConfig = storeConfigMapper.selectOne(
                    new LambdaQueryWrapper<StoreConfig>()
                            .eq(StoreConfig::getShopCode, dto.getShopCode())
                            .ne(StoreConfig::getId, config.getId())
            );
            if (existingConfig == null) {
                config.setShopCode(dto.getShopCode());
            }
        } else if (config.getShopCode() == null || config.getShopCode().isEmpty()) {
            // 如果原值为空，生成新的shopCode
            config.setShopCode(generateShopCode());
        }
        
        storeConfigMapper.updateById(config);
    }
}
