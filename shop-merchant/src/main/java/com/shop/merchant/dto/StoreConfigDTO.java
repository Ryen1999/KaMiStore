package com.shop.merchant.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 店铺配置数据传输对象
 * <p>
 * 用于接收前端提交的店铺配置更新请求参数。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Data
public class StoreConfigDTO {

    /** 店铺ID */
    private String id;

    /** 租户ID */
    private String tenantId;

    /** 店铺名称（必填） */
    @NotBlank(message = "店铺名称不能为空")
    private String storeName;

    /** 店铺Logo地址 */
    private String storeLogo;

    /** 店铺描述 */
    private String storeDesc;

    /** 店铺公告 */
    private String storeNotice;

    /** 自动弹窗公告 */
    private Boolean autoPopNotice;

    /** PC端样式 */
    private String pcStyle;

    /** 移动端样式 */
    private String mobileStyle;

    /** 背景音乐URL */
    private String bgMusicUrl;

    /** 主题颜色（十六进制） */
    private String themeColor;

    /** SEO标题 */
    private String seoTitle;

    /** SEO关键词 */
    private String seoKeywords;

    /** SEO描述 */
    private String seoDescription;

    /** 店铺启用状态 */
    private Boolean storeEnabled;

    /** 商家QQ */
    private String merchantQq;

    /** 群组链接 */
    private String groupLink;

    /** 模板ID */
    private String templateId;

    /** 装修JSON配置 */
    private String decorationJson;

    /** 创建时间 */
    private String createdAt;

    /** 更新时间 */
    private String updatedAt;

    /** 页脚HTML */
    private String footerHtml;

    /** 店铺唯一标识码 */
    private String shopCode;
}
