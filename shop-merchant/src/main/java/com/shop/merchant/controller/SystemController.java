package com.shop.merchant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.common.core.exception.BizException;
import com.shop.common.core.result.PageResult;
import com.shop.common.core.result.R;
import com.shop.merchant.dto.ChangePasswordDTO;
import com.shop.merchant.entity.NotifyMessage;
import com.shop.merchant.entity.OperationLog;
import com.shop.merchant.feign.AuthFeignClient;
import com.shop.merchant.mapper.NotifyMessageMapper;
import com.shop.merchant.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class SystemController {

    private final OperationLogMapper operationLogMapper;
    private final NotifyMessageMapper notifyMessageMapper;
    private final AuthFeignClient authFeignClient;

    @GetMapping("/login-log/page")
    public R<PageResult<OperationLog>> pageLoginLogs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationLog::getModule, "auth");
        wrapper.orderByDesc(OperationLog::getCreatedAt);
        Page<OperationLog> page = operationLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return R.ok(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @GetMapping("/message/page")
    public R<PageResult<NotifyMessage>> pageMessages(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        LambdaQueryWrapper<NotifyMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(NotifyMessage::getCreatedAt);
        Page<NotifyMessage> page = notifyMessageMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return R.ok(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @PutMapping("/message/read-all")
    public R<Void> readAllMessages() {
        LambdaUpdateWrapper<NotifyMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(NotifyMessage::getIsRead, 1).eq(NotifyMessage::getIsRead, 0);
        notifyMessageMapper.update(null, wrapper);
        return R.ok();
    }

    @PutMapping("/password")
    public R<Void> changePassword(@RequestBody ChangePasswordDTO dto) {
        R<Void> result = authFeignClient.changePassword(dto);
        if (result == null) {
            throw new BizException("修改密码失败：认证服务无响应");
        }
        if (!result.isSuccess()) {
            throw new BizException("修改密码失败：" + result.getMsg());
        }
        return R.ok();
    }
}
