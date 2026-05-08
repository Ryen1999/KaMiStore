package com.shop.merchant.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 商户角色数据传输对象
 * <p>
 * 用于接收前端提交的角色创建与更新请求参数。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Data
public class MerchantRoleDTO {

    /** 角色名称（必填） */
    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    /** 角色编码（必填，唯一标识） */
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    /** 权限列表（JSON格式） */
    private String permissions;
}
