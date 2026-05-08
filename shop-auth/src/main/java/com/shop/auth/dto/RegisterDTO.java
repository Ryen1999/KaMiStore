package com.shop.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 注册请求参数
 *
 * @author shop
 * @since 1.0.0
 */
@Data
public class RegisterDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 邮箱 */
    private String email;

    /** QQ号 */
    private String qq;

    /** 手机号 */
    private String phone;
}
