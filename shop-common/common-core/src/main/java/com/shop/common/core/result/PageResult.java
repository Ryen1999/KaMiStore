package com.shop.common.core.result;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页查询结果封装
 * <p>
 * 用于分页接口的统一响应格式，包含分页元数据和数据列表。
 * </p>
 *
 * @param <T> 数据项类型
 * @author shop
 * @since 1.0.0
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private long total;

    /** 当前页码 */
    private long pageNum;

    /** 每页大小 */
    private long pageSize;

    /** 数据列表 */
    private List<T> list;

    /**
     * 构建分页结果
     *
     * @param total    总记录数
     * @param pageNum  当前页码
     * @param pageSize 每页大小
     * @param list     数据列表
     * @param <T>      数据项类型
     * @return 分页结果对象
     */
    public static <T> PageResult<T> of(long total, long pageNum, long pageSize, List<T> list) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setList(list);
        return result;
    }
}
