package com.shop.merchant.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 结算配置数据传输对象
 *
 * @author shop
 * @since 1.0.0
 */
@Data
public class SettlementConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 收款类型：system-系统默认, manual-手工提现, auto-自动提现 */
    private String collectionType;

    /** 支付宝账号 */
    @NotBlank(message = "支付宝账号不能为空")
    private String alipayAccount;

    /** 收款人姓名 */
    @NotBlank(message = "收款人姓名不能为空")
    private String payeeName;

    /** 收款二维码URL */
    private String qrcodeUrl;
}
