package com.shop.merchant.controller;

import com.shop.merchant.entity.StoreConfig;
import com.shop.merchant.service.StoreConfigService;
import com.shop.common.core.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 店铺公开Controller
 * <p>
 * 提供公开店铺信息查询，无需登录即可访问。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@RestController
@RequestMapping("/public/shop")
@RequiredArgsConstructor
public class PublicShopController {

    private final StoreConfigService storeConfigService;

    /**
     * 根据shopCode获取店铺信息
     *
     * @param shopCode 店铺标识码
     * @return 店铺公开信息
     */
    @GetMapping("/info/{shopCode}")
    public R<ShopPublicVO> getShopInfo(@PathVariable String shopCode) {
        StoreConfig config = storeConfigService.getByShopCode(shopCode);
        if (config == null) {
            return R.fail("店铺不存在或已下架");
        }

        ShopPublicVO vo = new ShopPublicVO();
        vo.setShopCode(shopCode);
        vo.setStoreName(config.getStoreName());
        vo.setStoreLogo(config.getStoreLogo());
        vo.setStoreDesc(config.getStoreDesc());
        vo.setStoreNotice(config.getStoreNotice());
        vo.setThemeColor(config.getThemeColor());
        vo.setMerchantQq(config.getMerchantQq());
        vo.setGroupLink(config.getGroupLink());

        return R.ok(vo);
    }

    /**
     * 店铺公开信息VO
     */
    @lombok.Data
    public static class ShopPublicVO {
        /** 店铺标识码 */
        private String shopCode;
        /** 店铺名称 */
        private String storeName;
        /** 店铺Logo */
        private String storeLogo;
        /** 店铺描述 */
        private String storeDesc;
        /** 店铺公告 */
        private String storeNotice;
        /** 主题颜色 */
        private String themeColor;
        /** 商家QQ */
        private String merchantQq;
        /** 群组链接 */
        private String groupLink;
    }
}