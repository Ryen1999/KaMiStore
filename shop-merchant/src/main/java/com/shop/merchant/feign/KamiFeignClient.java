package com.shop.merchant.feign;

import com.shop.common.core.result.R;
import com.shop.merchant.feign.dto.KamiAllocateResultDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;

/**
 * 卡密服务Feign客户端
 * <p>
 * 远程调用shop-kami服务的内部接口，
 * 实现卡密锁定、确认分配、释放功能。
 * 所有接口通过X-Internal-Token进行内部鉴权。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@FeignClient(name = "shop-kami", path = "/internal/kami")
public interface KamiFeignClient {

    /**
     * 锁定卡密
     * <p>
     * 下单时预锁N条未售卡密，TTL内不会被其他订单抢占。
     * 锁定成功后卡密状态由0(未售)变为1(锁定)。
     * </p>
     *
     * @param token    内部鉴权令牌
     * @param lockDTO  锁定参数（tenantId, productId, skuId, quantity, orderNo, lockTtlSeconds）
     * @return 锁定的卡密ID列表
     */
    @PostMapping("/lock")
    R<List<Long>> lockKami(@RequestHeader("X-Internal-Token") String token,
                           @RequestBody Map<String, Object> lockDTO);

    /**
     * 确认卡密分配（锁定→已售）
     * <p>
     * 支付成功后调用，将锁定卡密转为已售状态，
     * 解密并返回卡密明文列表。
     * </p>
     *
     * @param token      内部鉴权令牌
     * @param confirmDTO 确认参数（tenantId, orderNo, kamiItemIds）
     * @return 卡密分配结果列表（含kamiItemId + 明文内容）
     */
    @PostMapping("/confirm")
    R<List<KamiAllocateResultDTO>> confirmKami(@RequestHeader("X-Internal-Token") String token,
                                                @RequestBody Map<String, Object> confirmDTO);

    /**
     * 释放过期卡密锁
     * <p>
     * 定时任务调用，将超时未支付订单的锁定卡密恢复为未售状态。
     * </p>
     *
     * @param token 内部鉴权令牌
     */
    @PostMapping("/release-expired")
    R<Integer> releaseExpired(@RequestHeader("X-Internal-Token") String token);
}
