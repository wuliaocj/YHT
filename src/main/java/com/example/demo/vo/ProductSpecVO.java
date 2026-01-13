package com.example.demo.vo;

import lombok.Data;

/**
 * 商品规格子VO（杯型/小料通用）
 */
@Data
public class ProductSpecVO {
    /** 规格ID */
    private Long specId;
    /** 关联商品ID */
    private Long productId;
    /** 规格名称（如大杯、珍珠） */
    private String specName;
    /** 加价金额 */
    private Double priceAdd;
    /** 规格状态（固定为1，查询时只返回启用的规格） */
    private Integer status = 1;
}
