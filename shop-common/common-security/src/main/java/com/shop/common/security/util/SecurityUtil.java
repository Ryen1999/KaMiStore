package com.shop.common.security.util;

import cn.dev33.satoken.stp.StpUtil;
import com.shop.common.core.constant.CommonConstant;

/**
 * 安全工具类
 * <p>
 * 封装Sa-Token常用操作，提供获取当前登录用户信息的便捷方法。
 * 业务层通过此工具类获取当前用户ID、用户类型等信息。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
public class SecurityUtil {

    private SecurityUtil() {
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    public static Long getUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    /**
     * 获取当前登录用户类型
     * <p>从Session中获取，登录时设置</p>
     *
     * @return 用户类型（admin/merchant）
     */
    public static String getUserType() {
        return (String) StpUtil.getSession().get(CommonConstant.HEADER_USER_TYPE);
    }

    /**
     * 判断当前用户是否为平台管理员
     *
     * @return true-是管理员
     */
    public static boolean isAdmin() {
        return CommonConstant.USER_TYPE_ADMIN.equals(getUserType());
    }

    /**
     * 判断当前用户是否为商家用户
     *
     * @return true-是商家
     */
    public static boolean isMerchant() {
        return CommonConstant.USER_TYPE_MERCHANT.equals(getUserType());
    }
}
