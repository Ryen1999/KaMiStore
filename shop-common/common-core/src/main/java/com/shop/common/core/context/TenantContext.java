package com.shop.common.core.context;

/**
 * 租户上下文持有器
 * <p>
 * 使用ThreadLocal存储当前请求的租户ID，在整个请求链路中传递。
 * Gateway从请求头X-Tenant-Id中提取租户ID并设置到此上下文中，
 * MyBatis-Plus多租户拦截器从此上下文获取租户ID自动追加WHERE条件。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
public class TenantContext {

    /** 租户ID线程变量 */
    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();

    /**
     * 设置当前线程的租户ID
     *
     * @param tenantId 租户ID
     */
    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    /**
     * 获取当前线程的租户ID
     *
     * @return 租户ID，未设置时返回null
     */
    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    /**
     * 清除当前线程的租户ID（请求结束时必须调用，防止内存泄漏）
     */
    public static void clear() {
        TENANT_ID.remove();
    }
}
