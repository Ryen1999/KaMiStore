package com.shop.merchant.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 修改密码请求参数
 *
 * @author shop
 * @since 1.0.0
 */
@Data
public class ChangePasswordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 旧密码 */
    private String oldPassword;

    /** 新密码 */
    private String newPassword;
}
