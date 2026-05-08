package com.shop.auth.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.auth.dto.LoginDTO;
import com.shop.auth.dto.LoginVO;
import com.shop.auth.dto.RegisterDTO;
import com.shop.auth.entity.Admin;
import com.shop.auth.entity.MerchantUser;
import com.shop.auth.entity.Tenant;
import com.shop.auth.mapper.AdminMapper;
import com.shop.auth.mapper.MerchantUserMapper;
import com.shop.auth.mapper.TenantMapper;
import com.shop.common.core.constant.CommonConstant;
import com.shop.common.core.enums.ResultCode;
import com.shop.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务
 * <p>
 * 统一处理平台管理员和商家用户的登录认证。
 * 登录流程：
 * 1. 根据userType查询对应用户表
 * 2. 校验密码（BCrypt）
 * 3. 校验账号状态
 * 4. Sa-Token登录，生成Token
 * 5. 将用户类型、租户ID等信息存入Session
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    /** 平台管理员Mapper */
    private final AdminMapper adminMapper;

    /** 商家用户Mapper */
    private final MerchantUserMapper merchantUserMapper;

    /** 租户Mapper */
    private final TenantMapper tenantMapper;

    /**
     * 统一登录接口
     * <p>
     * 根据userType区分平台管理员和商家用户，查询不同的用户表。
     * 登录成功后通过Sa-Token生成JWT Token。
     * </p>
     *
     * @param dto 登录参数（用户名、密码、用户类型）
     * @return 登录结果（Token、用户信息）
     */
    public LoginVO login(LoginDTO dto) {
        if (CommonConstant.USER_TYPE_ADMIN.equals(dto.getUserType())) {
            return loginAdmin(dto);
        } else if (CommonConstant.USER_TYPE_MERCHANT.equals(dto.getUserType())) {
            return loginMerchant(dto);
        } else {
            throw new BizException("不支持的用户类型: " + dto.getUserType());
        }
    }

    /**
     * 平台管理员登录
     *
     * @param dto 登录参数
     * @return 登录结果
     */
    private LoginVO loginAdmin(LoginDTO dto) {
        // 1. 查询管理员
        Admin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, dto.getUsername())
        );
        if (admin == null) {
            throw new BizException(ResultCode.AUTH_LOGIN_FAIL);
        }

        // 2. 校验密码
        String inputPasswordHash = BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt());
        log.info("========== 密码校验详情 ==========");
        log.info("用户名: {}", dto.getUsername());
        log.info("用户输入的明文密码: {}", dto.getPassword());
        log.info("输入密码的BCrypt密文: {}", inputPasswordHash);
        log.info("数据库中的BCrypt密文: {}", admin.getPassword());
        log.info("密码是否匹配: {}", BCrypt.checkpw(dto.getPassword(), admin.getPassword()));
        log.info("==================================");

        if (!BCrypt.checkpw(dto.getPassword(), admin.getPassword())) {
            throw new BizException(ResultCode.AUTH_LOGIN_FAIL);
        }

        // 3. 校验状态
        if (admin.getStatus() != CommonConstant.STATUS_ENABLE) {
            throw new BizException(ResultCode.AUTH_ACCOUNT_DISABLED);
        }

        // 4. Sa-Token登录（使用admin前缀避免ID冲突）
        String loginId = CommonConstant.USER_TYPE_ADMIN + ":" + admin.getId();
        StpUtil.login(loginId);

        // 5. 将用户类型存入Session
        StpUtil.getSession().set(CommonConstant.HEADER_USER_TYPE, CommonConstant.USER_TYPE_ADMIN);

        // 6. 构建响应
        return buildLoginVO(admin.getId(), admin.getUsername(), admin.getRealName(),
                CommonConstant.USER_TYPE_ADMIN, null);
    }

    /**
     * 商家用户登录
     *
     * @param dto 登录参数
     * @return 登录结果
     */
    private LoginVO loginMerchant(LoginDTO dto) {
        // 1. 查询商家用户
        MerchantUser user = merchantUserMapper.selectOne(
                new LambdaQueryWrapper<MerchantUser>()
                        .eq(MerchantUser::getUsername, dto.getUsername())
        );
        if (user == null) {
            throw new BizException(ResultCode.AUTH_LOGIN_FAIL);
        }

        // 2. 校验密码
        if (!BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BizException(ResultCode.AUTH_LOGIN_FAIL);
        }

        // 3. 校验状态
        if (user.getStatus() != CommonConstant.STATUS_ENABLE) {
            throw new BizException(ResultCode.AUTH_ACCOUNT_DISABLED);
        }

        // 4. Sa-Token登录
        String loginId = CommonConstant.USER_TYPE_MERCHANT + ":" + user.getId();
        StpUtil.login(loginId);

        // 5. 存入Session（包含用户类型和租户ID，供网关转发使用）
        StpUtil.getSession().set(CommonConstant.HEADER_USER_TYPE, CommonConstant.USER_TYPE_MERCHANT);
        StpUtil.getSession().set(CommonConstant.HEADER_TENANT_ID, user.getTenantId());

        // 6. 构建响应
        return buildLoginVO(user.getId(), user.getUsername(), user.getRealName(),
                CommonConstant.USER_TYPE_MERCHANT, user.getTenantId());
    }

    /**
     * 商户注册
     * <p>
     * 注册时自动创建租户记录，并将租户ID关联到用户。
     * </p>
     *
     * @param dto 注册参数
     */
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO dto) {
        // 1. 检查用户名是否已存在
        Long count = merchantUserMapper.selectCount(
                new LambdaQueryWrapper<MerchantUser>()
                        .eq(MerchantUser::getUsername, dto.getUsername())
        );
        if (count > 0) {
            throw new BizException("用户名已存在");
        }

        // 2. 创建租户记录
        Tenant tenant = new Tenant();
        tenant.setTenantName(dto.getUsername() + "的店铺");
        tenant.setContactPhone(dto.getPhone());
        tenant.setEmail(dto.getEmail());
        tenant.setStatus(CommonConstant.STATUS_ENABLE);
        tenant.setPlanType(1);
        tenantMapper.insert(tenant);

        // 3. 创建商家用户，关联租户ID
        MerchantUser user = new MerchantUser();
        user.setTenantId(tenant.getId());
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));
        user.setPhone(dto.getPhone());
        user.setIsOwner(1);
        user.setStatus(CommonConstant.STATUS_ENABLE);
        merchantUserMapper.insert(user);
    }

    /**
     * 退出登录
     */
    public void logout() {
        StpUtil.logout();
    }

    /**
     * 构建登录响应对象
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param realName 真实姓名
     * @param userType 用户类型
     * @param tenantId 租户ID
     * @return 登录响应
     */
    private LoginVO buildLoginVO(Long userId, String username, String realName,
                                 String userType, Long tenantId) {
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        LoginVO vo = new LoginVO();
        vo.setToken(tokenInfo.getTokenValue());
        vo.setExpireIn(tokenInfo.getTokenTimeout());
        vo.setUserId(userId);
        vo.setUsername(username);
        vo.setRealName(realName);
        vo.setUserType(userType);
        vo.setTenantId(tenantId);
        return vo;
    }
}
