package com.shop.merchant.controller;

import com.shop.common.core.result.R;
import com.shop.merchant.dto.StoreConfigDTO;
import com.shop.merchant.entity.StoreConfig;
import com.shop.merchant.service.StoreConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 店铺配置控制器
 * <p>
 * 提供店铺配置的查询和更新接口。
 * 接口路径统一以 /merchant/store 开头。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@RestController
@RequestMapping("/merchant/store")
@RequiredArgsConstructor
public class StoreConfigController {

    /** 店铺配置服务 */
    private final StoreConfigService storeConfigService;

    /**
     * 获取当前商户的店铺配置
     *
     * @return 店铺配置信息
     */
    @GetMapping
    public R<StoreConfig> getStoreConfig() {
        return R.ok(storeConfigService.getStoreConfig());
    }

    /**
     * 更新当前商户的店铺配置
     *
     * @param dto 店铺配置更新参数
     * @return 操作结果
     */
    @PutMapping
    public R<Void> updateStoreConfig(@RequestBody @Validated StoreConfigDTO dto) {
        storeConfigService.updateStoreConfig(dto);
        return R.ok();
    }

    /**
     * 获取当前商户的店铺链接信息
     *
     * @return 店铺链接信息，包含shopCode和shopLink
     */
    @GetMapping("/link")
    public R<ShopLinkVO> getShopLink() {
        StoreConfig config = storeConfigService.getStoreConfig();
        ShopLinkVO vo = new ShopLinkVO();
        vo.setShopCode(config.getShopCode());
        vo.setShopLink("/shop/" + (config.getShopCode() != null ? config.getShopCode() : ""));
        vo.setEnabled(config.getStoreEnabled() != null && config.getStoreEnabled() == 1);
        return R.ok(vo);
    }

    /**
     * 店铺链接VO
     */
    @lombok.Data
    public static class ShopLinkVO {
        /** 店铺标识码 */
        private String shopCode;
        /** 店铺访问路径 */
        private String shopLink;
        /** 店铺是否启用 */
        private Boolean enabled;
    }
}
