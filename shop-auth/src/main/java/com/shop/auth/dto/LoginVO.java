package com.shop.auth.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录响应结果
 *
 * @author shop
 * @since 1.0.0
 */
@Data
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 访问Token */
    private String token;

    /** Token过期时间（秒） */
    private long expireIn;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 用户类型 */
    private String userType;

    /** 租户ID（商家用户才有） */
    private Long tenantId;
}
