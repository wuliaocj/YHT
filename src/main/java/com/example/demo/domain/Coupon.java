package com.example.demo.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@TableName("coupon")
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

    // 核心修复：改为 LocalDate + 指定JSON格式
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate startTime;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate endTime;

    private Integer validDays;
    private String applicableProducts;
    private String applicableCategories;
    private Integer status;

    // LocalDateTime 需要带时分秒的格式
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
