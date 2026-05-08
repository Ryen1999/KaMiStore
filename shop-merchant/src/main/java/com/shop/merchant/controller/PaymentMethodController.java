package com.shop.merchant.controller;

import com.shop.common.core.result.R;
import com.shop.merchant.dto.PaymentMethodStatusDTO;
import com.shop.merchant.dto.PaymentMethodConfigDTO;
import com.shop.merchant.entity.PaymentMethod;
import com.shop.merchant.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 支付方式控制器
 * <p>
 * 提供支付方式的管理接口：列表查询、状态切换、配置更新。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@RestController
@RequestMapping("/store/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    /** 支付方式服务 */
    private final PaymentMethodService paymentMethodService;

    /**
     * 获取支付方式列表
     *
     * @return 支付方式列表
     */
    @GetMapping
    public R<List<PaymentMethod>> listMethods() {
        return R.ok(paymentMethodService.listMethods());
    }

    /**
     * 更新支付方式状态
     *
     * @param dto 状态更新参数
     * @return 操作结果
     */
    @PutMapping("/status")
    public R<Void> updateStatus(@RequestBody PaymentMethodStatusDTO dto) {
        paymentMethodService.updateStatus(dto.getMethodCode(), dto.getEnabled());
        return R.ok();
    }

    /**
     * 获取支付方式配置
     *
     * @param methodCode 支付方式代码
     * @return 支付方式配置
     */
    @GetMapping("/{methodCode}")
    public R<PaymentMethod> getConfig(@PathVariable String methodCode) {
        return R.ok(paymentMethodService.getConfig(methodCode));
    }

    /**
     * 更新支付方式配置
     *
     * @param methodCode 支付方式代码
     * @param dto        配置参数
     * @return 操作结果
     */
    @PutMapping("/{methodCode}")
    public R<Void> updateConfig(@PathVariable String methodCode, @RequestBody PaymentMethodConfigDTO dto) {
        paymentMethodService.updateConfig(methodCode, dto.getMerchantId(), dto.getApiKey(), dto.getPrivateKey());
        return R.ok();
    }
}