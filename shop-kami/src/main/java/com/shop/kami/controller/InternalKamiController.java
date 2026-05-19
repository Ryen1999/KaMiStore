package com.shop.kami.controller;

import com.shop.common.core.result.R;
import com.shop.kami.service.KamiAllocateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 卡密内部接口控制器
 * <p>
 * 提供卡密锁定、确认分配、释放过期锁的内部接口，
 * 供shop-merchant通过Feign调用。
 * 所有接口通过X-Internal-Token进行鉴权。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/internal/kami")
@RequiredArgsConstructor
public class InternalKamiController {

    /** 卡密分配服务 */
    private final KamiAllocateService kamiAllocateService;

    /**
     * 锁定卡密
     * <p>
     * 下单时预锁N条未售卡密，防止超卖。
     * 锁定后15分钟内未被确认支付则自动释放。
     * </p>
     *
     * @param token 内部鉴权令牌
     * @param dto   锁定参数
     * @return 锁定的卡密ID列表
     */
    @PostMapping("/lock")
    public R<List<Long>> lock(@RequestHeader("X-Internal-Token") String token,
                               @RequestBody Map<String, Object> dto) {
        Long tenantId = Long.valueOf(dto.get("tenantId").toString());
        Long productId = Long.valueOf(dto.get("productId").toString());
        Long skuId = Long.valueOf(dto.get("skuId").toString());
        int quantity = Integer.parseInt(dto.get("quantity").toString());
        String orderNo = (String) dto.get("orderNo");
        int lockTtlSeconds = Integer.parseInt(dto.get("lockTtlSeconds").toString());

        List<Long> ids = kamiAllocateService.lock(tenantId, productId, skuId, quantity, orderNo, lockTtlSeconds);
        return R.ok(ids);
    }

    /**
     * 确认卡密分配
     * <p>
     * 支付成功后调用，将锁定卡密转为已售状态并返回明文。
     * </p>
     *
     * @param token 内部鉴权令牌
     * @param dto   确认参数
     * @return 卡密分配结果列表
     */
    @PostMapping("/confirm")
    public R<List<Map<String, Object>>> confirm(@RequestHeader("X-Internal-Token") String token,
                                                  @RequestBody Map<String, Object> dto) {
        Long tenantId = Long.valueOf(dto.get("tenantId").toString());
        String orderNo = (String) dto.get("orderNo");

        List<Map<String, Object>> result = kamiAllocateService.confirm(tenantId, orderNo);
        return R.ok(result);
    }

    /**
     * 释放过期卡密锁
     * <p>
     * 定时任务调用，将超时未支付的锁定卡密恢复为未售状态。
     * </p>
     *
     * @param token 内部鉴权令牌
     * @return 释放的卡密数量
     */
    @PostMapping("/release-expired")
    public R<Integer> releaseExpired(@RequestHeader("X-Internal-Token") String token) {
        int count = kamiAllocateService.releaseExpired();
        return R.ok(count);
    }
}
