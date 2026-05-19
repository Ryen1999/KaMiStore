package com.shop.merchant.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.common.core.result.R;
import com.shop.merchant.entity.NotifyMessage;
import com.shop.merchant.entity.Order;
import com.shop.merchant.entity.ProfitSharingRecord;
import com.shop.merchant.entity.SettlementConfig;
import com.shop.merchant.feign.KamiFeignClient;
import com.shop.merchant.mapper.NotifyMessageMapper;
import com.shop.merchant.mapper.OrderMapper;
import com.shop.merchant.mapper.ProfitSharingRecordMapper;
import com.shop.merchant.mapper.SettlementConfigMapper;
import com.shop.merchant.service.AlipayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * T+1结算定时任务
 * <p>
 * 每日凌晨执行，完成两件事：
 * 1. 释放超时未支付的卡密锁（status=1且lockExpireAt已过期→status=0）
 * 2. 扫描T-1已支付未结算的订单，通过支付宝单笔转账将商家应得金额打给商家收款账号
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementJob {

    /** 订单Mapper */
    private final OrderMapper orderMapper;

    /** 结算配置Mapper */
    private final SettlementConfigMapper settlementConfigMapper;

    /** 分账记录Mapper */
    private final ProfitSharingRecordMapper profitSharingRecordMapper;

    /** 站内消息Mapper */
    private final NotifyMessageMapper notifyMessageMapper;

    /** 支付宝支付服务 */
    private final AlipayService alipayService;

    /** 卡密Feign客户端 */
    private final KamiFeignClient kamiFeignClient;

    /** 内部鉴权令牌 */
    @Value("${shop.internal-token:shop-internal}")
    private String internalToken;

    /**
     * T+1结算定时任务
     * <p>
     * cron从配置读取，默认每日00:30执行。
     * 只结算pay_time早于今日0点的订单（即T-1及之前支付的订单）。
     * </p>
     */
    @Scheduled(cron = "${shop.settle.cron:0 30 0 * * ?}")
    public void execute() {
        log.info("========== T+1结算任务开始 ==========");

        // 1. 释放过期卡密锁
        releaseExpiredLocks();

        // 2. 执行T+1结算
        settleOrders();

        log.info("========== T+1结算任务结束 ==========");
    }

    /**
     * 释放超时未支付的卡密锁
     */
    private void releaseExpiredLocks() {
        try {
            R<Integer> result = kamiFeignClient.releaseExpired(internalToken);
            if (result != null && result.isSuccess()) {
                log.info("过期卡密锁释放成功 count={}", result.getData());
            }
        } catch (Exception e) {
            log.error("释放过期卡密锁失败", e);
        }
    }

    /**
     * 扫描待结算订单并执行转账
     */
    private void settleOrders() {
        // T-1零点：（今日0点 - 不要结算今天刚支付的）
        LocalDateTime tMinusOneCutoff = LocalDate.now().atStartOfDay();

        List<Order> pendingOrders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, 1)
                        .eq(Order::getSettleStatus, 0)
                        .lt(Order::getPayTime, tMinusOneCutoff)
        );

        if (pendingOrders.isEmpty()) {
            log.info("无待结算订单");
            return;
        }

        log.info("扫描到{}笔待结算订单", pendingOrders.size());

        int successCount = 0;
        int failCount = 0;

        for (Order order : pendingOrders) {
            try {
                boolean settled = settleOneOrder(order);
                if (settled) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                failCount++;
                log.error("结算异常 orderNo={}, tenantId={}", order.getOrderNo(), order.getTenantId(), e);
            }
        }

        log.info("结算完成 成功={}, 失败={}", successCount, failCount);
    }

    /**
     * 结算单笔订单
     *
     * @param order 待结算订单
     * @return true-结算成功
     */
    private boolean settleOneOrder(Order order) {
        String orderNo = order.getOrderNo();
        Long tenantId = order.getTenantId();

        // 获取结算配置
        SettlementConfig config = settlementConfigMapper.selectOne(
                new LambdaQueryWrapper<SettlementConfig>()
                        .eq(SettlementConfig::getTenantId, tenantId));

        if (config == null || config.getAlipayAccount() == null
                || config.getAlipayAccount().trim().isEmpty()) {
            markSettleFailed(order, "商家未配置收款账号");
            return false;
        }

        // 生成转账单号（幂等）
        String outBizNo = "STL" + orderNo;
        String remark = order.getProductName() != null
                ? "商品[" + order.getProductName() + "]结算"
                : "店铺结算";

        Map<String, Object> transferResult = alipayService.uniTransfer(
                config.getAlipayAccount(),
                config.getPayeeName() != null ? config.getPayeeName() : "商家",
                order.getMerchantSettleAmount(),
                outBizNo,
                remark
        );

        if (Boolean.TRUE.equals(transferResult.get("success"))) {
            markSettleSuccess(order, (String) transferResult.get("tradeNo"));
            updateProfitSharingAfterSettle(orderNo, (String) transferResult.get("tradeNo"));
            return true;
        } else {
            String failReason = (String) transferResult.get("failReason");
            markSettleFailed(order, failReason != null ? failReason : "转账失败");
            return false;
        }
    }

    /**
     * 标记订单结算成功
     */
    private void markSettleSuccess(Order order, String tradeNo) {
        LocalDateTime now = LocalDateTime.now();
        order.setSettleStatus(1);
        order.setSettleTime(now);
        order.setSettleTradeNo(tradeNo);
        orderMapper.updateById(order);
        createSettlementMessage(order, "订单" + order.getOrderNo() + "结算成功",
                "结算金额：" + order.getMerchantSettleAmount() + "元已转入账户，感谢你的使用");
        log.info("订单结算成功 orderNo={}, settleTradeNo={}", order.getOrderNo(), tradeNo);
    }

    /**
     * 标记订单结算失败
     */
    private void markSettleFailed(Order order, String failReason) {
        order.setSettleStatus(2);
        order.setSettleFailReason(failReason);
        orderMapper.updateById(order);
        createSettlementMessage(order, "订单" + order.getOrderNo() + "结算失败",
                "结算失败原因：" + failReason + "，请检查收款配置后重试");
        log.warn("订单结算失败 orderNo={}, reason={}", order.getOrderNo(), failReason);
    }

    /**
     * 写入结算站内消息，写入失败不影响结算主流程。
     */
    private void createSettlementMessage(Order order, String title, String content) {
        try {
            NotifyMessage message = new NotifyMessage();
            message.setTenantId(order.getTenantId());
            message.setReceiverType(2);
            message.setTitle(title);
            message.setContent(content);
            message.setMsgType(2);
            message.setIsRead(0);
            message.setCreatedAt(LocalDateTime.now());
            notifyMessageMapper.insert(message);
        } catch (Exception e) {
            log.warn("站内消息写入失败 orderNo={}", order.getOrderNo(), e);
        }
    }

    /**
     * 结算成功后更新分账记录
     */
    private void updateProfitSharingAfterSettle(String orderNo, String tradeNo) {
        ProfitSharingRecord sharing = profitSharingRecordMapper.selectOne(
                new LambdaQueryWrapper<ProfitSharingRecord>()
                        .eq(ProfitSharingRecord::getOrderNo, orderNo));
        if (sharing != null) {
            sharing.setChannelSharingNo(tradeNo);
            sharing.setStatus(2);
            sharing.setSharedAt(LocalDateTime.now());
            profitSharingRecordMapper.updateById(sharing);
        }
    }
}
