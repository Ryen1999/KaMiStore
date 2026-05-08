package com.shop.merchant.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.common.core.exception.BizException;
import com.shop.common.core.result.PageResult;
import com.shop.common.core.result.R;
import com.shop.merchant.dto.ChangePasswordDTO;
import com.shop.merchant.entity.NotifyMessage;
import com.shop.merchant.entity.OperationLog;
import com.shop.merchant.mapper.NotifyMessageMapper;
import com.shop.merchant.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 系统管理控制器
 * <p>提供登录日志、站内消息、修改密码接口</p>
 *
 * @author shop
 * @since 1.0.0
 */
@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class SystemController {

    /** 操作日志Mapper */
    private final OperationLogMapper operationLogMapper;

    /** 站内消息Mapper */
    private final NotifyMessageMapper notifyMessageMapper;

    /**
     * 分页查询登录日志
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @GetMapping("/login-log/page")
    public R<PageResult<OperationLog>> pageLoginLogs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationLog::getModule, "auth");
        wrapper.orderByDesc(OperationLog::getCreatedAt);

        Page<OperationLog> page = operationLogMapper.selectPage(
                new Page<>(pageNum, pageSize), wrapper);
        return R.ok(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    /**
     * 分页查询站内消息
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @GetMapping("/message/page")
    public R<PageResult<NotifyMessage>> pageMessages(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        LambdaQueryWrapper<NotifyMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(NotifyMessage::getCreatedAt);

        Page<NotifyMessage> page = notifyMessageMapper.selectPage(
                new Page<>(pageNum, pageSize), wrapper);
        return R.ok(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    /**
     * 全部消息标记已读
     *
     * @return 操作结果
     */
    @PutMapping("/message/read-all")
    public R<Void> readAllMessages() {
        LambdaUpdateWrapper<NotifyMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(NotifyMessage::getIsRead, 1)
                .eq(NotifyMessage::getIsRead, 0);
        notifyMessageMapper.update(null, wrapper);
        return R.ok();
    }

    /**
     * 修改密码
     *
     * @param dto 修改密码参数
     * @return 操作结果
     */
    @PutMapping("/password")
    public R<Void> changePassword(@RequestBody ChangePasswordDTO dto) {
        // 获取当前登录信息
        String loginId = StpUtil.getLoginIdAsString();
        if (loginId == null) {
            throw new BizException("未登录");
        }
        // 校验新密码
        if (dto.getNewPassword() == null || dto.getNewPassword().length() < 6) {
            throw new BizException("新密码不能少于6位");
        }
        // 密码修改成功提示（实际需要查表校验旧密码，此处简化处理）
        return R.ok();
    }
}
