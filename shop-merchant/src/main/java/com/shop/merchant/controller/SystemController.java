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

import java.time.LocalDateTime;

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
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer isRead) {

        LambdaQueryWrapper<NotifyMessage> wrapper = new LambdaQueryWrapper<>();
        applyMerchantMessageScope(wrapper);
        if (isRead != null) {
            wrapper.eq(NotifyMessage::getIsRead, isRead);
        }
        wrapper.orderByDesc(NotifyMessage::getCreatedAt);
        Page<NotifyMessage> page = notifyMessageMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return R.ok(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @GetMapping("/message/unread-count")
    public R<Long> unreadMessageCount() {
        LambdaQueryWrapper<NotifyMessage> wrapper = new LambdaQueryWrapper<>();
        applyMerchantMessageScope(wrapper);
        wrapper.eq(NotifyMessage::getIsRead, 0);
        Long count = notifyMessageMapper.selectCount(wrapper);
        return R.ok(count);
    }

    @PutMapping("/message/read-all")
    public R<Void> readAllMessages() {
        LambdaUpdateWrapper<NotifyMessage> wrapper = new LambdaUpdateWrapper<>();
        applyMerchantMessageScope(wrapper);
        wrapper.set(NotifyMessage::getIsRead, 1)
                .set(NotifyMessage::getReadAt, LocalDateTime.now())
                .eq(NotifyMessage::getIsRead, 0);
        notifyMessageMapper.update(null, wrapper);
        return R.ok();
    }

    @PutMapping("/message/{id}/read-status")
    public R<Void> updateMessageReadStatus(
            @PathVariable Long id,
            @RequestParam Integer isRead) {

        int readStatus = Integer.valueOf(1).equals(isRead) ? 1 : 0;
        LambdaUpdateWrapper<NotifyMessage> wrapper = new LambdaUpdateWrapper<>();
        applyMerchantMessageScope(wrapper);
        wrapper.eq(NotifyMessage::getId, id)
                .set(NotifyMessage::getIsRead, readStatus)
                .set(NotifyMessage::getReadAt, readStatus == 1 ? LocalDateTime.now() : null);
        notifyMessageMapper.update(null, wrapper);
        return R.ok();
    }

    @DeleteMapping("/message/{id}")
    public R<Void> deleteMessage(@PathVariable Long id) {
        LambdaUpdateWrapper<NotifyMessage> wrapper = new LambdaUpdateWrapper<>();
        applyMerchantMessageScope(wrapper);
        wrapper.eq(NotifyMessage::getId, id);
        notifyMessageMapper.delete(wrapper);
        return R.ok();
    }

    private void applyMerchantMessageScope(LambdaQueryWrapper<NotifyMessage> wrapper) {
        wrapper.and(item -> item.eq(NotifyMessage::getReceiverType, 2)
                .or()
                .isNull(NotifyMessage::getReceiverType));
    }

    private void applyMerchantMessageScope(LambdaUpdateWrapper<NotifyMessage> wrapper) {
        wrapper.and(item -> item.eq(NotifyMessage::getReceiverType, 2)
                .or()
                .isNull(NotifyMessage::getReceiverType));
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
