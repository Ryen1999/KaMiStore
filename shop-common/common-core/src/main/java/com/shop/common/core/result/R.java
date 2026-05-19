package com.shop.common.core.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装
 * <p>
 * 所有接口统一返回此对象，前端根据code判断请求是否成功。
 * code=200表示成功，其他表示失败。
 * </p>
 *
 * @param <T> 响应数据类型
 * @author shop
 * @since 1.0.0
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码：200成功，其他失败 */
    private int code;

    /** 提示信息 */
    private String msg;

    /** 响应数据 */
    private T data;

    /** 成功状态码 */
    public static final int SUCCESS_CODE = 200;

    /** 失败状态码 */
    public static final int FAIL_CODE = 500;

    /**
     * 成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return 成功响应对象
     */
    public static <T> R<T> ok() {
        return ok(null);
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应对象
     */
    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(SUCCESS_CODE);
        r.setMsg("success");
        r.setData(data);
        return r;
    }

    /**
     * 失败响应（默认状态码500）
     *
     * @param msg 错误信息
     * @param <T> 数据类型
     * @return 失败响应对象
     */
    public static <T> R<T> fail(String msg) {
        return fail(FAIL_CODE, msg);
    }

    /**
     * 失败响应（自定义状态码）
     *
     * @param code 状态码
     * @param msg  错误信息
     * @param <T>  数据类型
     * @return 失败响应对象
     */
    public static <T> R<T> fail(int code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }

    /**
     * 判断是否成功
     *
     * @return true-成功 false-失败
     */
    @JsonIgnore
    public boolean isSuccess() {
        return this.code == SUCCESS_CODE;
    }

    /**
     * 兼容历史响应中的success字段。
     * <p>旧版本会把isSuccess()序列化成success，服务间Feign反序列化时需要接受该字段。</p>
     */
    public void setSuccess(Boolean success) {
        // success由code计算，不单独存储。
    }
}
