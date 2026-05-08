package com.shop.common.mybatis.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.shop.common.core.context.TenantContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 多租户SQL拦截处理器
 * <p>
 * MyBatis-Plus的TenantLineInnerInterceptor会调用此处理器，
 * 自动为所有SQL追加 WHERE tenant_id = ? 条件。
 * </p>
 * <p>
 * 核心逻辑：
 * 1. getTenantId() - 从TenantContext获取当前请求的租户ID
 * 2. getTenantIdColumn() - 指定租户字段名为 tenant_id
 * 3. ignoreTable() - 配置不需要租户隔离的表（如平台管理员表）
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
public class ShopTenantLineHandler implements TenantLineHandler {

    /**
     * 不需要租户隔离的表集合
     * <p>
     * 平台级别的表（管理员、角色、权限等）不属于任何租户，
     * 不应追加tenant_id条件。
     * </p>
     */
    private static final Set<String> IGNORE_TABLES = new HashSet<>(Arrays.asList(
            "t_admin",                  // 平台管理员
            "t_admin_role",             // 平台角色
            "t_admin_permission",       // 平台权限
            "t_admin_role_permission",  // 角色-权限关联
            "t_tenant",                 // 租户表本身
            "t_tenant_domain",          // 租户域名(通过domain查tenant_id，不能反向过滤)
            "t_member_level"            // 会员等级配置(后续可按需调整)
    ));

    /**
     * 获取当前请求的租户ID
     * <p>从ThreadLocal中获取，由Gateway注入到请求头，再由TenantInterceptor提取</p>
     *
     * @return 租户ID表达式，未设置时返回NullValue
     */
    @Override
    public Expression getTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            return new LongValue(tenantId);
        }
        // 未设置租户ID时返回NullValue，避免空指针
        return new NullValue();
    }

    /**
     * 获取租户字段名
     *
     * @return 固定返回 tenant_id
     */
    @Override
    public String getTenantIdColumn() {
        return "tenant_id";
    }

    /**
     * 判断是否忽略该表的租户隔离
     *
     * @param tableName 表名
     * @return true-忽略（不追加tenant_id条件） false-追加
     */
    @Override
    public boolean ignoreTable(String tableName) {
        return IGNORE_TABLES.contains(tableName);
    }
}
