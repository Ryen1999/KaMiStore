package com.shop.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 登录请求参数
 *
 * @author shop
 * @since 1.0.0
 */
@Data
public class LoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 用户类型：admin-平台管理员 merchant-商家用户
     * <p>用于区分不同登录入口，查询不同的用户表</p>
     */
    @NotBlank(message = "用户类型不能为空")
    private String userType;

    /** 租户ID（商家登录时需要，管理员登录不需要） */
    private Long tenantId;
}
