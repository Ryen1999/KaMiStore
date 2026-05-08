package com.shop.merchant.controller;

import com.shop.common.core.result.R;
import com.shop.merchant.dto.MerchantRoleDTO;
import com.shop.merchant.entity.MerchantRole;
import com.shop.merchant.service.MerchantRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商户角色控制器
 * <p>
 * 提供商户角色的增删改查接口。
 * 接口路径统一以 /merchant/role 开头。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@RestController
@RequestMapping("/merchant/role")
@RequiredArgsConstructor
public class MerchantRoleController {

    /** 商户角色服务 */
    private final MerchantRoleService merchantRoleService;

    /**
     * 查询当前租户的所有角色
     *
     * @return 角色列表
     */
    @GetMapping("/list")
    public R<List<MerchantRole>> listRoles() {
        return R.ok(merchantRoleService.listRoles());
    }

    /**
     * 创建角色
     *
     * @param dto 角色创建参数
     * @return 操作结果
     */
    @PostMapping
    public R<Void> createRole(@RequestBody @Validated MerchantRoleDTO dto) {
        merchantRoleService.createRole(dto);
        return R.ok();
    }

    /**
     * 更新角色
     *
     * @param id  角色ID
     * @param dto 角色更新参数
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public R<Void> updateRole(@PathVariable Long id,
                              @RequestBody @Validated MerchantRoleDTO dto) {
        merchantRoleService.updateRole(id, dto);
        return R.ok();
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public R<Void> deleteRole(@PathVariable Long id) {
        merchantRoleService.deleteRole(id);
        return R.ok();
    }
}
