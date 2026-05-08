package com.shop.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 连接保活过滤器
 * <p>
 * 强制设置响应头 Connection: keep-alive，
 * 解决Node.js HTTP代理(Vite dev proxy)因"Data after Connection: close"而报502的问题。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Component
public class KeepAliveFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        exchange.getResponse().getHeaders().set(HttpHeaders.CONNECTION, "keep-alive");
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -200;
    }
}
