package com.shop.auth.controller;

import com.shop.auth.dto.LoginDTO;
import com.shop.auth.dto.LoginVO;
import com.shop.auth.dto.RegisterDTO;
import com.shop.auth.service.AuthService;
import com.shop.common.core.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * <p>
 * 提供登录、登出接口，所有接口无需鉴权（在Gateway白名单中）。
 * 接口路径统一以 /auth 开头，Gateway路由到此服务。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    /** 认证服务 */
    private final AuthService authService;

    /**
     * 统一登录接口
     * <p>
     * 支持平台管理员和商家用户登录，通过userType区分。
     * 请求示例：
     * {
     *   "username": "admin",
     *   "password": "123456",
     *   "userType": "admin"
     * }
     * </p>
     *
     * @param dto 登录参数
     * @return Token和用户信息
     */
    @PostMapping("/login")
    public R<LoginVO> login(@RequestBody @Validated LoginDTO dto) {
        return R.ok(authService.login(dto));
    }

    /**
     * 商户注册接口
     *
     * @param dto 注册参数
     * @return 操作结果
     */
    @PostMapping("/register")
    public R<Void> register(@RequestBody @Validated RegisterDTO dto) {
        authService.register(dto);
        return R.ok();
    }

    /**
     * 退出登录
     * <p>注销当前Token，清除Sa-Token会话</p>
     *
     * @return 操作结果
     */
    @PostMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }
}
