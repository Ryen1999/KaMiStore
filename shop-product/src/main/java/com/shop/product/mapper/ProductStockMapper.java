package com.shop.product.mapper;

import com.shop.product.dto.ProductStockCountDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ProductStockMapper {

    @Select({
            "<script>",
            "SELECT product_id AS productId, COUNT(1) AS stock",
            "FROM t_kami_item",
            "WHERE tenant_id = #{tenantId}",
            "AND status = 0",
            "AND product_id IN",
            "<foreach collection='productIds' item='productId' open='(' separator=',' close=')'>",
            "#{productId}",
            "</foreach>",
            "GROUP BY product_id",
            "</script>"
    })
    List<ProductStockCountDTO> countAvailableStock(@Param("tenantId") Long tenantId,
                                                   @Param("productIds") List<Long> productIds);

    @Select({
            "SELECT COUNT(1)",
            "FROM t_kami_item",
            "WHERE tenant_id = #{tenantId}",
            "AND status = 0",
            "AND product_id = #{productId}"
    })
    Integer countAvailableStockByProductId(@Param("tenantId") Long tenantId,
                                           @Param("productId") Long productId);
}
