package com.shop.merchant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 *
 * @author shop
 * @since 1.0.0
 */
@Data
@TableName("t_operation_log")
public class OperationLog {

    /** 主键 */
    private Long id;

    /** 租户ID */
    private Long tenantId;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人姓名 */
    private String operatorName;

    /** 操作人类型 1平台管理员 2商家 */
    private Integer operatorType;

    /** 模块名 */
    private String module;

    /** 操作名 */
    private String action;

    /** 请求方法 */
    private String method;

    /** 请求URL */
    private String requestUrl;

    /** IP地址 */
    private String ip;

    /** 浏览器UA */
    private String userAgent;

    /** 耗时(ms) */
    private Integer costTime;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
