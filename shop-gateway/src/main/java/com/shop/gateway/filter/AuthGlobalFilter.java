package com.shop.gateway.filter;

import cn.dev33.satoken.reactor.context.SaReactorSyncHolder;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 全局鉴权过滤器
 * <p>
 * 在Gateway层统一校验Token，拦截未认证的请求。
 * 白名单路径（C端公开接口、登录接口等）直接放行。
 * 鉴权通过后将用户ID注入请求头，传递给下游微服务。
 * </p>
 * <p>
 * 注意：Gateway基于WebFlux，不能直接使用StpUtil.checkLogin()，
 * 需要手动从Header提取Token再通过StpUtil.getLoginIdByToken()校验。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    /** Token请求头名称 */
    private static final String HEADER_AUTHORIZATION = "Authorization";

    /** Token前缀 */
    private static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 白名单路径列表（无需登录即可访问）
     */
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/auth/",           // 登录/注册接口
            "/api/store/",          // C端公开接口
            "/doc.html",            // Knife4j接口文档
            "/webjars/",            // 静态资源
            "/swagger-resources",   // Swagger资源
            "/v2/api-docs",         // API文档
            "/actuator/"            // 健康检查
    );

    /**
     * 核心过滤逻辑
     * <p>
     * 1. 打印转发请求日志
     * 2. 白名单路径直接放行
     * 3. 从Header手动提取Token
     * 4. 通过Sa-Token的getLoginIdByToken校验Token有效性（异步执行）
     * 5. 鉴权通过后注入用户ID到请求头
     * </p>
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        String query = request.getURI().getQuery();
        String remoteAddr = request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress() : "unknown";

        log.info("========== Gateway转发日志 ==========");
        log.info("请求路径: {}", path);
        log.info("请求方法: {}", method);
        log.info("查询参数: {}", query != null ? query : "无");
        log.info("来源IP: {}", remoteAddr);
        log.info("完整URL: {}{}", path, query != null ? "?" + query : "");

        // 1. 白名单路径直接放行
        if (isWhitePath(path)) {
            return chain.filter(exchange);
        }

        // 2. 从Header手动提取Token
        String authorization = request.getHeaders().getFirst(HEADER_AUTHORIZATION);
        if (StrUtil.isBlank(authorization)) {
            log.warn("鉴权失败: 缺少Authorization头, path={}", path);
            return unauthorizedResponse(exchange);
        }

        // 3. 去除Bearer前缀，获取纯Token值
        String token = authorization;
        if (authorization.startsWith(TOKEN_PREFIX)) {
            token = authorization.substring(TOKEN_PREFIX.length());
        }

        // 保存最终token用于lambda表达式
        final String finalToken = token;

        // 4. 将同步的Sa-Token调用切换到弹性线程池异步执行，避免阻塞Netty事件循环线程
        return Mono.fromCallable(() -> {
                try {
                    // 设置Sa-Reactor上下文，使Sa-Token能在响应式环境中工作
                    SaReactorSyncHolder.setContext(exchange);
                    Object loginId = StpUtil.getLoginIdByToken(finalToken);
                    return loginId != null ? loginId.toString() : null;
                } finally {
                    SaReactorSyncHolder.clearContext();
                }
            })
            .subscribeOn(Schedulers.boundedElastic()) // 切换到弹性线程池执行同步操作
            .flatMap(userId -> {
                if (userId == null) {
                    log.warn("鉴权失败: Token无效或已过期, path={}", path);
                    return unauthorizedResponse(exchange);
                }

                // 5. 鉴权通过，将用户ID和租户ID注入请求头传递给下游服务
                ServerHttpRequest.Builder reqBuilder = request.mutate()
                        .header("X-User-Id", userId);

                // 从Sa-Token Session获取租户ID并注入请求头
                try {
                    SaReactorSyncHolder.setContext(exchange);
                    Object tenantId = StpUtil.getSessionByLoginId(
                            StpUtil.getLoginIdByToken(finalToken)).get("X-Tenant-Id");
                    if (tenantId != null) {
                        reqBuilder.header("X-Tenant-Id", tenantId.toString());
                    }
                } catch (Exception e) {
                    log.debug("获取租户ID失败: {}", e.getMessage());
                } finally {
                    SaReactorSyncHolder.clearContext();
                }

                return chain.filter(exchange.mutate().request(reqBuilder.build()).build());
            })
            .onErrorResume(e -> {
                log.warn("鉴权失败, path={}, error={}", path, e.getMessage());
                return unauthorizedResponse(exchange);
            });
    }

    /**
     * 判断请求路径是否在白名单中
     *
     * @param path 请求路径
     * @return true-在白名单中，无需鉴权
     */
    private boolean isWhitePath(String path) {
        return WHITE_LIST.stream().anyMatch(path::contains);
    }

    /**
     * 返回401未认证响应
     *
     * @param exchange 请求上下文
     * @return 响应
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"code\":401,\"msg\":\"未认证，请先登录\",\"data\":null}";
        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 过滤器执行顺序（数值越小越优先）
     * <p>租户过滤器(-100)先执行，鉴权过滤器(-90)后执行</p>
     */
    @Override
    public int getOrder() {
        return -90;
    }
}
