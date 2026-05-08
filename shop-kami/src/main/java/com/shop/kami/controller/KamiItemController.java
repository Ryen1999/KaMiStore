package com.shop.kami.controller;

import com.shop.common.core.result.PageResult;
import com.shop.common.core.result.R;
import com.shop.kami.dto.KamiImportDTO;
import com.shop.kami.dto.KamiItemQueryDTO;
import com.shop.kami.entity.KamiImportBatch;
import com.shop.kami.entity.KamiItem;
import com.shop.kami.service.KamiItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 卡密条目管理接口
 * <p>
 * 提供卡密条目的查询、导入、作废和明文查看接口，包括：
 * 1. 分页查询卡密列表（不含明文内容）
 * 2. 批量导入卡密（加密存储、去重校验）
 * 3. 作废卡密
 * 4. 查看卡密明文（管理用途）
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@RestController
@RequestMapping("/kami/item")
@RequiredArgsConstructor
public class KamiItemController {

    /** 卡密条目服务 */
    private final KamiItemService kamiItemService;

    /**
     * 分页查询卡密列表
     * <p>
     * 出于安全考虑，列表接口不返回卡密明文内容。
     * </p>
     *
     * @param queryDTO 查询条件（卡密组ID、状态、分页参数）
     * @return 分页结果（不含卡密明文）
     */
    @GetMapping("/page")
    public R<PageResult<KamiItem>> pageItems(KamiItemQueryDTO queryDTO) {
        return R.ok(kamiItemService.pageItems(queryDTO));
    }

    /**
     * 批量导入卡密
     * <p>
     * 支持通过API方式批量导入卡密内容，
     * 自动进行SHA256去重和AES加密存储。
     * </p>
     *
     * @param dto 导入参数（卡密组ID、卡密内容列表）
     * @return 导入结果（成功数、失败数、失败明细）
     */
    @PostMapping("/import")
    public R<KamiImportBatch> importKami(@Validated @RequestBody KamiImportDTO dto) {
        return R.ok(kamiItemService.importKami(dto));
    }

    /**
     * 作废卡密
     *
     * @param id     卡密条目ID
     * @param reason 作废原因
     * @return 操作结果
     */
    @PutMapping("/{id}/void")
    public R<Void> voidKami(@PathVariable Long id, @RequestParam String reason) {
        kamiItemService.voidKami(id, reason);
        return R.ok();
    }

    /**
     * 查看卡密明文
     * <p>
     * 解密并返回卡密明文内容，仅供管理后台使用。
     * </p>
     *
     * @param id 卡密条目ID
     * @return 解密后的卡密明文
     */
    @GetMapping("/{id}/content")
    public R<String> getKamiContent(@PathVariable Long id) {
        return R.ok(kamiItemService.getKamiContent(id));
    }
}
