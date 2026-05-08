package com.shop.common.core.constant;

/**
 * 系统公共常量
 * <p>
 * 定义全局通用的常量值，避免魔法值硬编码。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
public class CommonConstant {

    private CommonConstant() {
    }

    // ==================== 请求头常量 ====================

    /** 租户ID请求头 */
    public static final String HEADER_TENANT_ID = "X-Tenant-Id";

    /** 用户ID请求头（网关鉴权后注入） */
    public static final String HEADER_USER_ID = "X-User-Id";

    /** 用户类型请求头 */
    public static final String HEADER_USER_TYPE = "X-User-Type";

    /** Token请求头 */
    public static final String HEADER_AUTHORIZATION = "Authorization";

    /** Token前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";

    // ==================== 用户类型 ====================

    /** 用户类型：平台管理员 */
    public static final String USER_TYPE_ADMIN = "admin";

    /** 用户类型：商家用户 */
    public static final String USER_TYPE_MERCHANT = "merchant";

    // ==================== 状态常量 ====================

    /** 状态：正常/启用 */
    public static final int STATUS_ENABLE = 1;

    /** 状态：禁用 */
    public static final int STATUS_DISABLE = 0;

    /** 逻辑删除：未删除 */
    public static final int NOT_DELETED = 0;

    /** 逻辑删除：已删除 */
    public static final int DELETED = 1;

    // ==================== 分页默认值 ====================

    /** 默认页码 */
    public static final int DEFAULT_PAGE_NUM = 1;

    /** 默认每页大小 */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /** 最大每页大小（防止一次查询过多） */
    public static final int MAX_PAGE_SIZE = 100;
}
