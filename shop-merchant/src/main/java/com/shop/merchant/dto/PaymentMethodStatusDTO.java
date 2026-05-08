package com.shop.merchant.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 支付方式状态更新DTO
 *
 * @author shop
 * @since 1.0.0
 */
@Data
public class PaymentMethodStatusDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 支付方式代码 */
    @NotBlank(message = "支付方式代码不能为空")
    private String methodCode;

    /** 是否启用 */
    private Boolean enabled;
}