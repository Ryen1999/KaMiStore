package com.shop.common.core.util;

import com.shop.common.core.enums.ResultCode;
import com.shop.common.core.exception.BizException;

/**
 * 业务断言工具类
 * <p>
 * 用于简化业务校验代码，校验不通过时直接抛出BizException。
 * 替代繁琐的if-throw写法。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
public class AssertUtil {

    private AssertUtil() {
    }

    /**
     * 断言对象不为null，为null则抛出业务异常
     *
     * @param obj        被检查的对象
     * @param resultCode 错误码枚举
     */
    public static void notNull(Object obj, ResultCode resultCode) {
        if (obj == null) {
            throw new BizException(resultCode);
        }
    }

    /**
     * 断言对象不为null，为null则抛出指定消息的业务异常
     *
     * @param obj 被检查的对象
     * @param msg 错误信息
     */
    public static void notNull(Object obj, String msg) {
        if (obj == null) {
            throw new BizException(msg);
        }
    }

    /**
     * 断言条件为true，不满足则抛出业务异常
     *
     * @param condition  条件表达式
     * @param resultCode 错误码枚举
     */
    public static void isTrue(boolean condition, ResultCode resultCode) {
        if (!condition) {
            throw new BizException(resultCode);
        }
    }

    /**
     * 断言条件为true，不满足则抛出指定消息的业务异常
     *
     * @param condition 条件表达式
     * @param msg       错误信息
     */
    public static void isTrue(boolean condition, String msg) {
        if (!condition) {
            throw new BizException(msg);
        }
    }
}
