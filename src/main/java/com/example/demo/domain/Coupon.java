package com.example.demo.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券表
 */
@Data
public class Coupon {

    private Integer id;
    private String name;
    private Integer type;
    private BigDecimal value;
    private BigDecimal minAmount;
    private Integer totalCount;
    private Integer remainingCount;
    private Integer limitPerUser;
    private Integer validityType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer validDays;
    private String applicableProducts;
    private String applicableCategories;
    private Integer status;
    private LocalDateTime createTime;
}


