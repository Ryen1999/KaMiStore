package com.shop.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.common.core.context.TenantContext;
import com.shop.common.core.exception.BizException;
import com.shop.merchant.entity.PaymentMethod;
import com.shop.merchant.mapper.PaymentMethodMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * 支付方式服务
 * <p>
 * 提供支付方式的管理功能：列表查询、状态切换、配置更新。
 * 系统初始化时自动创建默认支付方式。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    /** 支付方式Mapper */
    private final PaymentMethodMapper paymentMethodMapper;

    /**
     * 系统初始化时创建默认支付方式
     */
    @PostConstruct
    public void initDefaultMethods() {
        Long tenantId = 1001L; // 默认租户ID
        
        // 检查是否已存在所有默认支付方式（通过检查其中一个来判断）
        LambdaQueryWrapper<PaymentMethod> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentMethod::getTenantId, tenantId);
        wrapper.eq(PaymentMethod::getMethodCode, "alipay_scan");
        PaymentMethod existing = paymentMethodMapper.selectOne(wrapper);
        
        if (existing != null) {
            log.info("租户 {} 已存在支付方式配置（{}），跳过初始化", tenantId, existing.getMethodName());
            return;
        }

        log.info("开始初始化默认支付方式...");
        List<PaymentMethod> defaultMethods = new ArrayList<>();
        
        // 支付宝扫码
        PaymentMethod alipayScan = new PaymentMethod();
        alipayScan.setTenantId(tenantId);
        alipayScan.setMethodCode("alipay_scan");
        alipayScan.setMethodName("支付宝扫码");
        alipayScan.setIcon("Monitor");
        alipayScan.setIconBg("rgba(0, 65, 216, 0.08)");
        alipayScan.setIconColor("var(--primary)");
        alipayScan.setDescription("通过支付宝二维码收款，支持移动端和桌面POS终端。");
        alipayScan.setFeeRate("0.60");
        alipayScan.setDailyLimit(50);
        alipayScan.setEnabled(true);
        defaultMethods.add(alipayScan);

        // 支付宝H5
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

        // 微信支付
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

        defaultMethods.forEach(paymentMethodMapper::insert);
        log.info("默认支付方式初始化完成，共 {} 条", defaultMethods.size());
    }

    /**
     * 获取支付方式列表
     *
     * @return 支付方式列表
     */
    public List<PaymentMethod> listMethods() {
        LambdaQueryWrapper<PaymentMethod> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentMethod::getTenantId, TenantContext.getTenantId());
        wrapper.orderByAsc(PaymentMethod::getCreatedAt);
        return paymentMethodMapper.selectList(wrapper);
    }

    /**
     * 更新支付方式状态
     *
     * @param methodCode 支付方式代码
     * @param enabled    是否启用
     */
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
        log.info("更新支付方式状态成功, methodCode={}, enabled={}", methodCode, enabled);
    }

    /**
     * 获取支付方式配置
     *
     * @param methodCode 支付方式代码
     * @return 支付方式实体
     */
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

    /**
     * 更新支付方式配置
     *
     * @param methodCode 支付方式代码
     * @param merchantId 商户号
     * @param apiKey     API密钥
     * @param privateKey 私钥
     */
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
        log.info("更新支付方式配置成功, methodCode={}", methodCode);
    }
}