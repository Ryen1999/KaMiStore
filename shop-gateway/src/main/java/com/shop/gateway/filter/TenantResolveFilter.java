package com.shop.gateway.filter;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 租户识别全局过滤器
 * <p>
 * 在Gateway层识别当前请求所属的租户，注入X-Tenant-Id请求头。
 * 识别方式优先级：
 * 1. 请求头已携带X-Tenant-Id（管理后台/商家后台请求）
 * 2. 根据请求域名从Redis查询域名→租户映射（C端独立域名请求）
 * </p>
 * <p>
 * Gateway基于WebFlux响应式架构，必须使用ReactiveRedisTemplate。
 * 此过滤器优先级最高（-100），确保租户ID在鉴权过滤器之前注入。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantResolveFilter implements GlobalFilter, Ordered {

    /** 响应式Redis模板（Gateway基于WebFlux，不能用阻塞式RedisTemplate） */
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    /** Redis中域名→租户映射的key前缀 */
    private static final String DOMAIN_KEY_PREFIX = "domain:";

    /**
     * 核心过滤逻辑：识别租户ID并注入请求头
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 1. 优先从请求头获取X-Tenant-Id
        String tenantId = request.getHeaders().getFirst("X-Tenant-Id");

        // 2. 请求头已有租户ID，直接放行
        if (StrUtil.isNotBlank(tenantId)) {
            return chain.filter(exchange);
        }

        // 3. 根据域名从Redis异步查询租户映射
        String host = request.getURI().getHost();
        if (StrUtil.isBlank(host)) {
            return chain.filter(exchange);
        }

        return reactiveRedisTemplate.opsForValue()
                .get(DOMAIN_KEY_PREFIX + host)
                .flatMap(cachedTenantId -> {
                    log.debug("域名解析租户, host={}, tenantId={}", host, cachedTenantId);
                    // 将租户ID注入请求头，传递给下游服务
                    ServerHttpRequest mutatedRequest = request.mutate()
                            .header("X-Tenant-Id", cachedTenantId)
                            .build();
                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                })
                // Redis中未查到映射，直接放行（可能是平台管理后台的请求）
                .switchIfEmpty(chain.filter(exchange));
    }

    /**
     * 最高优先级（-100），确保在鉴权过滤器之前执行
     */
    @Override
    public int getOrder() {
        return -100;
    }
}
