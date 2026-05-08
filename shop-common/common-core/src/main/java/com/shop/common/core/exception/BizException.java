package com.shop.common.core.exception;

import com.shop.common.core.enums.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 * <p>
 * 业务逻辑中需要中断流程并返回错误信息时抛出此异常，
 * 由全局异常处理器捕获并转换为统一响应格式返回给前端。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Getter
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 错误码 */
    private final int code;

    /**
     * 通过错误码枚举创建业务异常
     *
     * @param resultCode 错误码枚举
     */
    public BizException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }

    /**
     * 自定义错误信息创建业务异常
     *
     * @param code 错误码
     * @param msg  错误信息
     */
    public BizException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    /**
     * 使用默认500错误码创建业务异常
     *
     * @param msg 错误信息
     */
    public BizException(String msg) {
        super(msg);
        this.code = ResultCode.INTERNAL_ERROR.getCode();
    }
}
