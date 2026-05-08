package com.shop.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 平台管理员实体
 *
 * @author shop
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_admin")
public class Admin extends BaseEntity {

    /** 用户名 */
    private String username;

    /** 密码(BCrypt加密) */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 头像URL */
    private String avatar;

    /** 角色ID */
    private Long roleId;

    /** 状态：0禁用 1正常 */
    private Integer status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginAt;
}
