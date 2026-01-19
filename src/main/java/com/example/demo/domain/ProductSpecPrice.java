package com.example.demo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 商品规格加价表
 * @TableName product_spec_price
 */
@TableName(value ="product_spec_price")
@Data
public class ProductSpecPrice {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联商品表id（对应product.id）
     */
    private Long productId;

    /**
     * 规格类型（cup_type=杯型，topping=小料）
     */
    private String specType;

    /**
     * 规格名称（大杯/珍珠/椰果等）
     */
    private String specName;

    /**
     * 加价金额（正数加价，负数减价，0不变）
     */
    private BigDecimal priceAdd;

    /**
     * 状态：1=可用，0=不可用
     */
    private Integer status;

    /**
     * 创建时间
     */
    // 核心修复：改为 LocalDate + 指定JSON格式
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    // 核心修复：改为 LocalDate + 指定JSON格式
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime updateTime;


}
