package com.shop.merchant.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayFundTransUniTransferRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayFundTransUniTransferResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.shop.common.core.exception.BizException;
import com.shop.merchant.config.AlipayProperties;
import com.shop.merchant.dto.AlipayPrepayVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝支付服务
 * <p>
 * 提供支付宝预支付下单、异步回调验签、单笔转账等核心能力。
 * 对接支付宝沙箱环境，使用RSA2签名。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlipayService {

    /** 支付宝客户端 */
    private final AlipayClient alipayClient;

    /** 支付宝配置属性 */
    private final AlipayProperties alipayProperties;

    /**
     * 创建支付宝预支付订单（当面付扫码）
     * <p>
     * 调用alipay.trade.precreate接口生成预支付订单，
     * 返回二维码链接及支付地址，供前端渲染扫码。
     * </p>
     *
     * @param orderNo 平台订单号
     * @param amount  支付金额（元）
     * @param subject 商品名称摘要
     * @return 预支付结果（qrCode、payUrl等）
     */
    public AlipayPrepayVO createPrePay(String orderNo, BigDecimal amount, String subject) {
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        if (!StringUtils.hasText(alipayProperties.getNotifyUrl())) {
            throw new BizException("支付宝notify-url未配置");
        }
        request.setNotifyUrl(alipayProperties.getNotifyUrl());
        log.info("alipay prepay request orderNo={}, notifyUrl={}", orderNo, alipayProperties.getNotifyUrl());

        JSONObject bizContent = new JSONObject();
        bizContent.set("out_trade_no", orderNo);
        bizContent.set("total_amount", amount.setScale(2, java.math.RoundingMode.HALF_UP).toString());
        bizContent.set("subject", subject);
        bizContent.set("timeout_express", "15m");
        request.setBizContent(bizContent.toString());

        try {
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                log.error("支付宝预下单失败 orderNo={}, code={}, msg={}", orderNo, response.getCode(), response.getMsg());
                throw new BizException("创建支付订单失败：" + response.getSubMsg());
            }

            AlipayPrepayVO vo = new AlipayPrepayVO();
            vo.setOrderNo(orderNo);
            vo.setQrCode(response.getQrCode());
            vo.setOutTradeNo(response.getOutTradeNo());
            vo.setAmount(amount);
            log.info("支付宝预下单成功 orderNo={}, outTradeNo={}", orderNo, response.getOutTradeNo());
            return vo;
        } catch (AlipayApiException e) {
            log.error("支付宝预下单异常 orderNo={}", orderNo, e);
            throw new BizException("支付宝通信异常：" + e.getErrMsg());
        }
    }

    /**
     * 验证并解析支付宝异步通知参数
     * <p>
     * 使用支付宝公钥验证请求签名，防止伪造回调。
     * 签名验证通过后将通知参数封装为Map返回。
     * </p>
     *
     * @param params 请求参数Map
     * @return 解析后的通知参数Map
     */
    public Map<String, String> verifyAndParseNotify(Map<String, String> params) {
        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayProperties.getPublicKey(),
                    alipayProperties.getCharset(),
                    alipayProperties.getSignType()
            );

            if (!signVerified) {
                log.error("支付宝回调验签失败 params={}", JSONUtil.toJsonStr(params));
                throw new BizException("支付宝回调签名验证失败");
            }

            String tradeStatus = params.get("trade_status");
            if (!"TRADE_SUCCESS".equals(tradeStatus)) {
                log.warn("支付宝回调非成功状态 tradeStatus={}", tradeStatus);
                throw new BizException("支付状态异常：" + tradeStatus);
            }

            log.info("支付宝回调验签成功 outTradeNo={}, tradeNo={}", params.get("out_trade_no"), params.get("trade_no"));
            return new HashMap<>(params);
        } catch (AlipayApiException e) {
            log.error("支付宝回调验签异常", e);
            throw new BizException("支付宝验签异常：" + e.getErrMsg());
        }
    }

    public Map<String, String> queryPaidTrade(String orderNo) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.set("out_trade_no", orderNo);
        request.setBizContent(bizContent.toString());

        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                log.info("alipay trade query not success orderNo={}, code={}, subCode={}, subMsg={}",
                        orderNo, response.getCode(), response.getSubCode(), response.getSubMsg());
                return null;
            }

            String tradeStatus = response.getTradeStatus();
            if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
                log.info("alipay trade not paid orderNo={}, tradeStatus={}", orderNo, tradeStatus);
                return null;
            }

            Map<String, String> result = new HashMap<>(8);
            result.put("out_trade_no", response.getOutTradeNo());
            result.put("trade_no", response.getTradeNo());
            result.put("trade_status", tradeStatus);
            result.put("total_amount", response.getTotalAmount());
            result.put("buyer_logon_id", response.getBuyerLogonId());
            return result;
        } catch (AlipayApiException e) {
            log.error("alipay trade query exception orderNo={}", orderNo, e);
            throw new BizException("支付宝查单异常：" + e.getErrMsg());
        }
    }

    /**
     * 支付宝单笔转账（用于T+1结算打款给商家）
     * <p>
     * 调用alipay.fund.trans.uni.transfer接口，
     * 将平台收款转至商家收款账号。
     * 使用人民币交易，走CHECK_QUALITY_PAY通道。
     * </p>
     *
     * @param payeeAccount 收款方支付宝账号
     * @param payeeName    收款方真实姓名
     * @param amount       转账金额（元）
     * @param outBizNo     外部转账单号（幂等控制）
     * @param remark       转账备注
     * @return 转账结果：{"success": true/false, "tradeNo": "支付宝单号"}
     */
    public Map<String, Object> uniTransfer(String payeeAccount, String payeeName,
                                           BigDecimal amount, String outBizNo, String remark) {
        AlipayFundTransUniTransferRequest request = new AlipayFundTransUniTransferRequest();

        JSONObject bizContent = new JSONObject();
        bizContent.set("out_biz_no", outBizNo);
        bizContent.set("trans_amount", amount.setScale(2, java.math.RoundingMode.HALF_UP).toString());
        bizContent.set("product_code", "TRANS_ACCOUNT_NO_PWD");
        bizContent.set("biz_scene", "DIRECT_TRANSFER");
        bizContent.set("order_title", remark != null ? remark : "T+1店铺结算");
        bizContent.set("remark", remark != null ? remark : "平台结算");

        JSONObject payeeInfo = new JSONObject();
        payeeInfo.set("identity", payeeAccount);
        payeeInfo.set("identity_type", "ALIPAY_LOGON_ID");
        payeeInfo.set("name", payeeName);
        bizContent.set("payee_info", payeeInfo);

        request.setBizContent(bizContent.toString());

        try {
            AlipayFundTransUniTransferResponse response = alipayClient.certificateExecute(request);
            Map<String, Object> result = new HashMap<>(4);
            result.put("success", response.isSuccess());
            if (response.isSuccess()) {
                result.put("tradeNo", response.getOrderId());
                result.put("outBizNo", response.getOutBizNo());
                log.info("支付宝转账成功 outBizNo={}, orderId={}", outBizNo, response.getOrderId());
            } else {
                result.put("failReason", response.getSubMsg());
                log.error("支付宝转账失败 outBizNo={}, code={}, subMsg={}",
                        outBizNo, response.getCode(), response.getSubMsg());
            }
            return result;
        } catch (AlipayApiException e) {
            log.error("支付宝转账异常 outBizNo={}", outBizNo, e);
            Map<String, Object> errorResult = new HashMap<>(2);
            errorResult.put("success", false);
            errorResult.put("failReason", e.getErrMsg());
            return errorResult;
        }
    }
}
