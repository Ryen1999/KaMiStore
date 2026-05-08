package com.shop.kami.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 卡密条目分页查询请求DTO
 * <p>
 * 用于接收分页查询卡密条目的请求参数，
 * 可指定商品ID和状态过滤。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Data
public class KamiItemQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 商品ID（可选） */
    private Long productId;

    /**
     * 卡密状态过滤
     * <ul>
     *   <li>0 - 未售</li>
     *   <li>1 - 锁定</li>
     *   <li>2 - 已售</li>
     *   <li>3 - 作废</li>
     * </ul>
     */
    private Integer status;

    /** 当前页码，默认第1页 */
    private Integer pageNum = 1;

    /** 每页大小，默认10条 */
    private Integer pageSize = 10;
}