package com.shop.common.core.config;

import com.shop.common.core.constant.CommonConstant;
import com.shop.common.core.context.TenantContext;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 租户上下文拦截器（Web MVC层）
 * <p>
 * 从请求头X-Tenant-Id中提取租户ID，设置到TenantContext中，
 * 供后续MyBatis-Plus多租户拦截器使用。
 * 请求结束后自动清理ThreadLocal，防止内存泄漏。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Slf4j
public class TenantInterceptor implements HandlerInterceptor {

    /**
     * 请求前：提取租户ID并设置到上下文
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantIdStr = request.getHeader(CommonConstant.HEADER_TENANT_ID);
        if (StrUtil.isNotBlank(tenantIdStr)) {
            try {
                TenantContext.setTenantId(Long.parseLong(tenantIdStr));
            } catch (NumberFormatException e) {
                log.warn("无效的租户ID: {}", tenantIdStr);
            }
        }
        return true;
    }

    /**
     * 请求后：清理ThreadLocal，防止线程复用导致数据串租户
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        TenantContext.clear();
    }
}
