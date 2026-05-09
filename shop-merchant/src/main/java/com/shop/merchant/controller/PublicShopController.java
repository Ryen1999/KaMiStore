package com.shop.merchant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.merchant.entity.Order;
import com.shop.merchant.entity.StoreConfig;
import com.shop.merchant.mapper.OrderMapper;
import com.shop.merchant.service.StoreConfigService;
import com.shop.common.core.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
    private final OrderMapper orderMapper;

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
        vo.setTenantId(config.getTenantId());

        return R.ok(vo);
    }

    /**
     * 根据订单号查询订单（公开接口）
     *
     * @param orderNo 订单号
     * @return 订单信息
     */
    @GetMapping("/order/{orderNo}")
    public R<Map<String, Object>> getOrderByNo(@PathVariable String orderNo) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        Order order = orderMapper.selectOne(wrapper);
        
        if (order == null) {
            return R.fail("订单不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", order.getOrderNo());
        result.put("status", order.getStatus());
        result.put("productName", "商品"); // TODO: 关联商品表查询
        result.put("quantity", 1); // TODO: 从订单明细获取
        result.put("amount", order.getPayAmount() != null ? order.getPayAmount() : order.getTotalAmount());
        result.put("paymentMethod", getPaymentMethodName(order.getPayMethod()));
        result.put("contact", maskContact(order.getContactValue()));
        result.put("createdAt", order.getCreatedAt());

        return R.ok(result);
    }

    /**
     * 获取支付方式名称
     */
    private String getPaymentMethodName(Integer payMethod) {
        if (payMethod == null) return "未支付";
        switch (payMethod) {
            case 1: return "微信支付";
            case 2: return "支付宝";
            case 3: return "QQ钱包";
            case 4: return "银行卡";
            default: return "其他支付";
        }
    }

    /**
     * 脱敏联系方式
     */
    private String maskContact(String contact) {
        if (contact == null || contact.length() <= 3) return contact;
        return contact.substring(0, 3) + "****" + contact.substring(contact.length() - 2);
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
        /** 租户ID */
        private Long tenantId;
    }
}