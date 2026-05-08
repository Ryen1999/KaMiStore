package com.shop.merchant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 店铺配置实体
 * <p>
 * 对应数据库表 t_store_config，存储商户的店铺装修与SEO配置信息。
 * 每个租户拥有一条店铺配置记录。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_store_config")
public class StoreConfig extends BaseEntity {

    /** 租户ID */
    private Long tenantId;

    /** 店铺名称 */
    private String storeName;

    /** 店铺Logo地址 */
    private String storeLogo;

    /** 店铺描述 */
    private String storeDesc;

    /** 店铺公告 */
    private String storeNotice;

    /** 主题颜色（十六进制） */
    private String themeColor;

    /** 店铺启用状态 */
    private Integer storeEnabled;

    /** 商家QQ */
    private String merchantQq;

    /** 群组链接 */
    private String groupLink;

    /** 自动弹窗公告 */
    private Integer autoPopNotice;

    /** PC端样式 */
    private String pcStyle;

    /** 移动端样式 */
    private String mobileStyle;

    /** 背景音乐URL */
    private String bgMusicUrl;

    /** 模板ID */
    private Long templateId;

    /** 装修配置JSON */
    private String decorationJson;

    /** SEO标题 */
    private String seoTitle;

    /** SEO关键词 */
    private String seoKeywords;

    /** SEO描述 */
    private String seoDescription;

    /** 页脚HTML */
    private String footerHtml;

    /** 店铺唯一标识码（用于生成店铺链接，每个商户唯一） */
    private String shopCode;
}
