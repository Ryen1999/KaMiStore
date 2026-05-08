package com.shop.common.core.exception;

import com.shop.common.core.enums.ResultCode;
import com.shop.common.core.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 统一捕获Controller层抛出的异常，转换为标准的R响应格式。
 * 处理顺序：业务异常 → 参数校验异常 → Spring框架异常 → 兜底异常。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     * <p>业务逻辑主动抛出的异常，直接返回异常中的code和msg</p>
     */
    @ExceptionHandler(BizException.class)
    public R<Void> handleBizException(BizException e) {
        log.warn("业务异常: code={}, msg={}", e.getCode(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常 - @RequestBody中的@Valid校验失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleValidException(MethodArgumentNotValidException e) {
        // 拼接所有字段校验错误信息
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", msg);
        return R.fail(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    /**
     * 参数校验异常 - 表单绑定校验失败
     */
    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException e) {
        String msg = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败: {}", msg);
        return R.fail(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    /**
     * 参数校验异常 - @RequestParam等单个参数校验失败
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public R<Void> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("约束校验失败: {}", msg);
        return R.fail(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    /**
     * 缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public R<Void> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数: {}", e.getParameterName());
        return R.fail(ResultCode.BAD_REQUEST.getCode(), "缺少参数: " + e.getParameterName());
    }

    /**
     * 请求方法不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("不支持的请求方法: {}", e.getMethod());
        return R.fail(ResultCode.BAD_REQUEST.getCode(), "不支持的请求方法: " + e.getMethod());
    }

    /**
     * 404 - 接口不存在
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public R<Void> handleNotFound(NoHandlerFoundException e) {
        return R.fail(ResultCode.NOT_FOUND.getCode(), "接口不存在: " + e.getRequestURL());
    }

    /**
     * 兜底异常处理 - 未被上述handler捕获的所有异常
     * <p>生产环境不暴露具体异常信息，只返回"系统繁忙"</p>
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return R.fail(ResultCode.INTERNAL_ERROR.getCode(), ResultCode.INTERNAL_ERROR.getMsg());
    }
}
