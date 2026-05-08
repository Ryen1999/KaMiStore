package com.shop.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一错误码枚举
 * <p>
 * 按模块分段定义错误码：
 * 200     - 成功
 * 400-499 - 客户端错误（参数校验、权限等）
 * 500-599 - 服务端错误
 * 1xxx    - 认证模块
 * 2xxx    - 商品模块
 * 3xxx    - 卡密模块
 * 4xxx    - 订单模块
 * 5xxx    - 支付模块
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // ==================== 通用 ====================
    /** 成功 */
    SUCCESS(200, "操作成功"),
    /** 请求参数错误 */
    BAD_REQUEST(400, "请求参数错误"),
    /** 未认证（未登录或Token过期） */
    UNAUTHORIZED(401, "未认证，请先登录"),
    /** 无权限访问 */
    FORBIDDEN(403, "权限不足"),
    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),
    /** 请求过于频繁 */
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后重试"),
    /** 系统内部错误 */
    INTERNAL_ERROR(500, "系统繁忙，请稍后重试"),

    // ==================== 认证模块 1xxx ====================
    /** 用户名或密码错误 */
    AUTH_LOGIN_FAIL(1001, "用户名或密码错误"),
    /** 账号已被禁用 */
    AUTH_ACCOUNT_DISABLED(1002, "账号已被禁用"),
    /** Token无效 */
    AUTH_TOKEN_INVALID(1003, "Token无效或已过期"),
    /** 验证码错误 */
    AUTH_CAPTCHA_ERROR(1004, "验证码错误"),

    // ==================== 租户模块 ====================
    /** 租户不存在 */
    TENANT_NOT_FOUND(1101, "租户不存在"),
    /** 租户已被禁用 */
    TENANT_DISABLED(1102, "店铺已被禁用"),
    /** 租户套餐已过期 */
    TENANT_PLAN_EXPIRED(1103, "套餐已过期"),

    // ==================== 卡密模块 3xxx ====================
    /** 卡密库存不足 */
    KAMI_STOCK_EMPTY(3001, "卡密库存不足"),
    /** 卡密发货失败 */
    KAMI_DELIVER_FAIL(3002, "卡密发货失败，请联系客服"),
    /** 卡密重复导入 */
    KAMI_DUPLICATE(3003, "存在重复卡密"),

    // ==================== 订单模块 4xxx ====================
    /** 订单不存在 */
    ORDER_NOT_FOUND(4001, "订单不存在"),
    /** 订单已关闭 */
    ORDER_CLOSED(4002, "订单已关闭"),
    /** 订单已支付 */
    ORDER_ALREADY_PAID(4003, "订单已支付，请勿重复操作"),

    // ==================== 支付模块 5xxx ====================
    /** 支付创建失败 */
    PAY_CREATE_FAIL(5001, "支付创建失败"),
    /** 支付回调验签失败 */
    PAY_CALLBACK_VERIFY_FAIL(5002, "支付回调验签失败");

    /** 错误码 */
    private final int code;

    /** 错误信息 */
    private final String msg;
}
