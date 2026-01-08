package com.example.demo.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车表
 */
@Data
public class Cart {

    private Integer id;
    private Integer userId;
    private Integer productId;
    private Integer quantity;
    private String selectedSpecs;
    private BigDecimal totalPrice;
    private Integer isSelected;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}


