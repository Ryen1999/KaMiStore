package com.shop.kami.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shop.common.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 卡密导入批次实体
 * <p>
 * 对应数据库表 t_kami_import_batch，记录每次卡密导入的批次信息，
 * 包括导入文件、处理结果、成功/失败数量等。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_kami_import_batch")
public class KamiImportBatch extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 租户ID */
    private Long tenantId;

    /** 所属商品ID */
    private Long productId;

    /** 导入文件名 */
    private String fileName;

    /** 导入文件URL */
    private String fileUrl;

    /** 文件类型（如xlsx、csv、txt） */
    private String fileType;

    /** 总条数 */
    private Integer totalCount;

    /** 成功条数 */
    private Integer successCount;

    /** 失败条数 */
    private Integer failCount;

    /** 失败明细（JSON格式） */
    private String failDetail;

    /**
     * 处理状态
     * <ul>
     *   <li>0 - 处理中</li>
     *   <li>1 - 成功</li>
     *   <li>2 - 部分失败</li>
     *   <li>3 - 全部失败</li>
     * </ul>
     */
    private Integer status;

    /** 操作人ID */
    private Long operatorId;

    /** 开始处理时间 */
    private LocalDateTime startedAt;

    /** 完成处理时间 */
    private LocalDateTime finishedAt;
}
