package com.shop.merchant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.common.core.context.TenantContext;
import com.shop.common.core.result.PageResult;
import com.shop.common.core.result.R;
import com.shop.common.oss.service.OssService;
import com.shop.merchant.dto.SettlementConfigDTO;
import com.shop.merchant.entity.SettlementConfig;
import com.shop.merchant.entity.WithdrawApply;
import com.shop.merchant.mapper.WithdrawApplyMapper;
import com.shop.merchant.service.SettlementConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 结算控制器
 * <p>提供提现申请、提现列表查询、结算概览、结算配置接口</p>
 *
 * @author shop
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/settlement")
@RequiredArgsConstructor
@Validated
public class SettlementController {

    /** 提现申请Mapper */
    private final WithdrawApplyMapper withdrawApplyMapper;

    /** 结算配置服务 */
    private final SettlementConfigService settlementConfigService;

    /** OSS服务 */
    private final OssService ossService;

    /**
     * 分页查询提现列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param status   状态(可选)
     * @return 分页结果
     */
    @GetMapping("/withdrawal/page")
    public R<PageResult<WithdrawApply>> pageWithdrawals(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<WithdrawApply> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(WithdrawApply::getStatus, status);
        }
        wrapper.orderByDesc(WithdrawApply::getCreatedAt);

        Page<WithdrawApply> page = withdrawApplyMapper.selectPage(
                new Page<>(pageNum, pageSize), wrapper);
        return R.ok(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    /**
     * 申请提现
     *
     * @param apply 提现申请参数
     * @return 操作结果
     */
    @PostMapping("/withdrawal/apply")
    public R<Void> applyWithdrawal(@RequestBody WithdrawApply apply) {
        // 设置租户ID
        apply.setTenantId(TenantContext.getTenantId());
        // 生成提现编号
        apply.setWithdrawNo("WD" + System.currentTimeMillis());
        // 默认手续费为2%
        BigDecimal fee = apply.getAmount().multiply(new BigDecimal("0.02"));
        apply.setFee(fee);
        apply.setActualAmount(apply.getAmount().subtract(fee));
        apply.setStatus(0);
        withdrawApplyMapper.insert(apply);
        return R.ok();
    }

    /**
     * 获取结算概览数据
     *
     * @return 概览数据
     */
    @GetMapping("/overview")
    public R<Map<String, Object>> getOverview() {
        Map<String, Object> data = new HashMap<>(4);
        data.put("balance", BigDecimal.ZERO);
        data.put("totalIncome", BigDecimal.ZERO);
        data.put("totalWithdraw", BigDecimal.ZERO);
        data.put("frozen", BigDecimal.ZERO);
        return R.ok(data);
    }

    /**
     * 获取结算配置
     *
     * @return 结算配置
     */
    @GetMapping("/config")
    public R<SettlementConfig> getConfig() {
        return R.ok(settlementConfigService.getConfig());
    }

    /**
     * 更新结算配置
     *
     * @param dto 结算配置参数
     * @return 操作结果
     */
    @PutMapping("/config")
    public R<Void> updateConfig(@Validated @RequestBody SettlementConfigDTO dto) {
        settlementConfigService.updateConfig(
                dto.getCollectionType(),
                dto.getAlipayAccount(),
                dto.getPayeeName(),
                dto.getQrcodeUrl()
        );
        return R.ok();
    }

    /**
     * 上传收款二维码
     *
     * @param file 二维码文件
     * @return 文件URL
     */
    @PostMapping("/upload/qrcode")
    public R<Map<String, String>> uploadQrcode(@RequestParam("file") MultipartFile file) {
        // 校验文件
        if (file == null || file.isEmpty()) {
            return R.fail("文件不能为空");
        }
        
        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            return R.fail("仅支持JPG/PNG格式的图片");
        }
        
        // 校验文件大小（2MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            return R.fail("文件大小不能超过10MB");
        }
        
        try {
            String url;
            // 尝试使用OSS上传
            try {
                url = ossService.upload(file, "qrcode");
            } catch (RuntimeException e) {
                // OSS未配置，使用本地存储（临时方案）
                log.warn("OSS服务未配置，使用本地文件存储: {}", e.getMessage());
                url = saveToLocal(file, "qrcode");
            }
            
            Map<String, String> result = new HashMap<>(1);
            result.put("url", url);
            return R.ok(result);
        } catch (Exception e) {
            log.error("上传失败", e);
            return R.fail("上传失败: " + e.getMessage());
        }
    }

    /**
     * 本地文件存储（临时方案）
     */
    private String saveToLocal(MultipartFile file, String dir) throws Exception {
        String uploadDir = System.getProperty("user.dir") + "/uploads/" + dir;
        java.io.File directory = new java.io.File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        java.io.File dest = new java.io.File(uploadDir + "/" + filename);
        file.transferTo(dest);
        
        // 返回相对路径
        return "/uploads/" + dir + "/" + filename;
    }

    /**
     * 上传收款二维码（兼容 /merchant/upload/qrcode 路径）
     *
     * @param file 二维码文件
     * @return 文件URL
     */
    @PostMapping(value = "/upload/qrcode", headers = "X-Forwarded-Prefix=/merchant")
    public R<Map<String, String>> uploadQrcodeForMerchant(@RequestParam("file") MultipartFile file) {
        return uploadQrcode(file);
    }
}
