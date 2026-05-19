package com.shop.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.common.core.context.TenantContext;
import com.shop.common.core.exception.BizException;
import com.shop.merchant.entity.PaymentMethod;
import com.shop.merchant.mapper.PaymentMethodMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    private final PaymentMethodMapper paymentMethodMapper;

    @PostConstruct
    public void initDefaultMethods() {
        Long tenantId = 1001L;
        for (PaymentMethod method : buildDefaultMethods(tenantId)) {
            initDefaultMethodIfAbsent(method);
        }
    }

    private void initDefaultMethodIfAbsent(PaymentMethod method) {
        LambdaQueryWrapper<PaymentMethod> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentMethod::getTenantId, method.getTenantId());
        wrapper.eq(PaymentMethod::getMethodCode, method.getMethodCode());

        PaymentMethod existing = paymentMethodMapper.selectOne(wrapper);
        if (existing != null) {
            log.info("default payment method already exists, tenantId={}, methodCode={}",
                    method.getTenantId(), method.getMethodCode());
            return;
        }

        try {
            paymentMethodMapper.insert(method);
            log.info("default payment method initialized, tenantId={}, methodCode={}",
                    method.getTenantId(), method.getMethodCode());
        } catch (DuplicateKeyException e) {
            log.info("default payment method initialized by another startup, tenantId={}, methodCode={}",
                    method.getTenantId(), method.getMethodCode());
        }
    }

    private List<PaymentMethod> buildDefaultMethods(Long tenantId) {
        List<PaymentMethod> defaultMethods = new ArrayList<>();

        PaymentMethod alipayScan = new PaymentMethod();
        alipayScan.setTenantId(tenantId);
        alipayScan.setMethodCode("alipay_scan");
        alipayScan.setMethodName("支付宝扫码");
        alipayScan.setIcon("Monitor");
        alipayScan.setIconBg("rgba(0, 65, 216, 0.08)");
        alipayScan.setIconColor("var(--primary)");
        alipayScan.setDescription("通过支付宝二维码收款，支持移动端和桌面终端。");
        alipayScan.setFeeRate("0.60");
        alipayScan.setDailyLimit(50);
        alipayScan.setEnabled(true);
        defaultMethods.add(alipayScan);

        PaymentMethod alipayH5 = new PaymentMethod();
        alipayH5.setTenantId(tenantId);
        alipayH5.setMethodCode("alipay_h5");
        alipayH5.setMethodName("支付宝H5");
        alipayH5.setIcon("Iphone");
        alipayH5.setIconBg("rgba(0, 65, 216, 0.08)");
        alipayH5.setIconColor("var(--primary)");
        alipayH5.setDescription("移动网页内嵌支付方案，适用于手机浏览器和外部应用集成。");
        alipayH5.setFeeRate("0.75");
        alipayH5.setDailyLimit(30);
        alipayH5.setEnabled(true);
        defaultMethods.add(alipayH5);

        PaymentMethod wechatPay = new PaymentMethod();
        wechatPay.setTenantId(tenantId);
        wechatPay.setMethodCode("wechat_pay");
        wechatPay.setMethodName("微信支付");
        wechatPay.setIcon("ChatDotRound");
        wechatPay.setIconBg("rgba(0, 107, 92, 0.08)");
        wechatPay.setIconColor("var(--secondary)");
        wechatPay.setDescription("微信生态直连支付，支持小程序、扫码和H5模式。");
        wechatPay.setFeeRate("0.60");
        wechatPay.setDailyLimit(40);
        wechatPay.setEnabled(true);
        defaultMethods.add(wechatPay);

        return defaultMethods;
    }

    public List<PaymentMethod> listMethods() {
        LambdaQueryWrapper<PaymentMethod> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentMethod::getTenantId, TenantContext.getTenantId());
        wrapper.orderByAsc(PaymentMethod::getCreatedAt);
        return paymentMethodMapper.selectList(wrapper);
    }

    public List<PaymentMethod> listEnabledMethodsByTenantId(Long tenantId) {
        LambdaQueryWrapper<PaymentMethod> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentMethod::getTenantId, tenantId);
        wrapper.eq(PaymentMethod::getEnabled, true);
        wrapper.orderByAsc(PaymentMethod::getCreatedAt);
        return paymentMethodMapper.selectList(wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(String methodCode, Boolean enabled) {
        LambdaQueryWrapper<PaymentMethod> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentMethod::getTenantId, TenantContext.getTenantId());
        wrapper.eq(PaymentMethod::getMethodCode, methodCode);
        PaymentMethod method = paymentMethodMapper.selectOne(wrapper);

        if (method == null) {
            throw new BizException("支付方式不存在");
        }

        method.setEnabled(enabled);
        paymentMethodMapper.updateById(method);
        log.info("payment method status updated, methodCode={}, enabled={}", methodCode, enabled);
    }

    public PaymentMethod getConfig(String methodCode) {
        LambdaQueryWrapper<PaymentMethod> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentMethod::getTenantId, TenantContext.getTenantId());
        wrapper.eq(PaymentMethod::getMethodCode, methodCode);
        PaymentMethod method = paymentMethodMapper.selectOne(wrapper);

        if (method == null) {
            throw new BizException("支付方式不存在");
        }

        return method;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(String methodCode, String merchantId, String apiKey, String privateKey) {
        LambdaQueryWrapper<PaymentMethod> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentMethod::getTenantId, TenantContext.getTenantId());
        wrapper.eq(PaymentMethod::getMethodCode, methodCode);
        PaymentMethod method = paymentMethodMapper.selectOne(wrapper);

        if (method == null) {
            throw new BizException("支付方式不存在");
        }

        method.setMerchantId(merchantId);
        method.setApiKey(apiKey);
        method.setPrivateKey(privateKey);
        paymentMethodMapper.updateById(method);
        log.info("payment method config updated, methodCode={}", methodCode);
    }
}
