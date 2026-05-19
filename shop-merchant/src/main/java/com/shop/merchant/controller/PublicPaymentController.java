package com.shop.merchant.controller;

import cn.hutool.json.JSONUtil;
import com.shop.common.core.result.R;
import com.shop.merchant.dto.AlipayPrepayVO;
import com.shop.merchant.dto.PublicOrderVO;
import com.shop.merchant.service.AlipayService;
import com.shop.merchant.service.PublicOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 公开支付控制器
 * <p>
 * 提供支付宝预支付下单和异步回调接收接口，
 * 面向买家无需认证。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping({"/public/shop", "/api/public/shop"})
@RequiredArgsConstructor
public class PublicPaymentController {

    /** 支付宝支付服务 */
    private final AlipayService alipayService;

    /** 公开订单服务 */
    private final PublicOrderService publicOrderService;

    /**
     * 创建支付宝预支付订单
     * <p>
     * 根据已创建的订单号生成支付宝当面付二维码，
     * 返回qrCode链接供前端渲染扫码。
     * </p>
     *
     * @param orderNo 平台订单号
     * @return 预支付信息（qrCode、支付金额等）
     */
    @PostMapping("/order/{orderNo}/alipay/prepay")
    public R<AlipayPrepayVO> createAlipayPrepay(@PathVariable String orderNo) {
        AlipayPrepayVO result = publicOrderService.createAlipayPrepay(orderNo);
        return R.ok(result);
    }

    @PostMapping("/order/{orderNo}/alipay/sync")
    public R<PublicOrderVO> syncAlipayPayment(@PathVariable String orderNo) {
        return R.ok(publicOrderService.syncAlipayPayment(orderNo));
    }

    /**
     * 支付宝异步通知回调
     * <p>
     * 接收支付宝异步支付结果通知，验签后更新订单状态。
     * 必须返回"success"字符串通知支付宝停止重试。
     * </p>
     *
     * @param request HTTP请求
     * @return "success"或"failure"
     */
    @PostMapping("/alipay/notify")
    public String alipayNotify(HttpServletRequest request) {
        try {
            Map<String, String> params = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                if (values != null && values.length > 0) {
                    params.put(key, values[0]);
                }
            });

            log.info("支付宝异步通知 params={}", JSONUtil.toJsonStr(params));

            Map<String, String> verifiedParams = alipayService.verifyAndParseNotify(params);
            String orderNo = verifiedParams.get("out_trade_no");
            String tradeNo = verifiedParams.get("trade_no");

            publicOrderService.markPaidByCallback(orderNo, tradeNo, JSONUtil.toJsonStr(verifiedParams));

            return "success";
        } catch (Exception e) {
            log.error("支付宝回调处理失败", e);
            return "failure";
        }
    }
}
