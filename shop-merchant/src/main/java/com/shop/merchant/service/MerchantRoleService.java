package com.shop.merchant.service;

import com.shop.common.core.context.TenantContext;
import com.shop.merchant.dto.MerchantRoleDTO;
import com.shop.merchant.entity.MerchantRole;
import com.shop.merchant.mapper.MerchantRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商户角色服务
 * <p>
 * 提供商户角色的增删改查功能。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class MerchantRoleService {

    /** 商户角色Mapper */
    private final MerchantRoleMapper merchantRoleMapper;

    /**
     * 查询所有角色
     *
     * @return 角色列表
     */
    public List<MerchantRole> listRoles() {
        return merchantRoleMapper.selectList(null);
    }

    /**
     * 创建角色
     *
     * @param dto 角色创建参数
     */
    public void createRole(MerchantRoleDTO dto) {
        MerchantRole role = new MerchantRole();
        role.setTenantId(TenantContext.getTenantId());
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setPermissions(dto.getPermissions());
        merchantRoleMapper.insert(role);
    }

    /**
     * 更新角色
     *
     * @param id  角色ID
     * @param dto 角色更新参数
     */
    public void updateRole(Long id, MerchantRoleDTO dto) {
        MerchantRole role = merchantRoleMapper.selectById(id);
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setPermissions(dto.getPermissions());
        merchantRoleMapper.updateById(role);
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    public void deleteRole(Long id) {
        merchantRoleMapper.deleteById(id);
    }
}
