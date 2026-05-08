package com.shop.kami.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 卡密导入请求DTO
 * <p>
 * 用于接收批量导入卡密的请求参数，
 * 包含目标商品ID和卡密内容列表。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Data
public class KamiImportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 目标商品ID（必填） */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /** 卡密内容列表（必填，不能为空列表） */
    @NotEmpty(message = "卡密内容列表不能为空")
    private List<String> kamiList;
}