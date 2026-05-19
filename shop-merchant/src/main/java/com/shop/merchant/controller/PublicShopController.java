package com.shop.merchant.controller;

import com.shop.common.core.result.R;
import com.shop.merchant.dto.PublicCreateOrderDTO;
import com.shop.merchant.dto.PublicOrderVO;
import com.shop.merchant.entity.PaymentMethod;
import com.shop.merchant.entity.StoreConfig;
import com.shop.merchant.service.PaymentMethodService;
import com.shop.merchant.service.PublicOrderService;
import com.shop.merchant.service.StoreConfigService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/public/shop", "/api/public/shop"})
@RequiredArgsConstructor
public class PublicShopController {

    private final StoreConfigService storeConfigService;
    private final PaymentMethodService paymentMethodService;
    private final PublicOrderService publicOrderService;

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
        vo.setTenantId(config.getTenantId());
        return R.ok(vo);
    }

    @GetMapping("/payment-methods/{shopCode}")
    public R<List<PaymentMethod>> getPaymentMethods(@PathVariable String shopCode) {
        StoreConfig config = storeConfigService.getByShopCode(shopCode);
        if (config == null) {
            return R.fail("店铺不存在或已下架");
        }
        return R.ok(paymentMethodService.listEnabledMethodsByTenantId(config.getTenantId()));
    }

    @PostMapping("/order")
    public R<PublicOrderVO> createOrder(@RequestBody PublicCreateOrderDTO dto) {
        return R.ok(publicOrderService.createOrder(dto));
    }

    @GetMapping("/order/{orderNo}")
    public R<PublicOrderVO> getOrderByNo(@PathVariable String orderNo,
                                         @RequestParam(required = false) String contactValue) {
        return R.ok(publicOrderService.queryOrder(orderNo, contactValue));
    }

    @Data
    public static class ShopPublicVO {
        private String shopCode;
        private String storeName;
        private String storeLogo;
        private String storeDesc;
        private String storeNotice;
        private String themeColor;
        private String merchantQq;
        private String groupLink;
        private Long tenantId;
    }
}
