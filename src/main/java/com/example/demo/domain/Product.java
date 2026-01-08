package com.example.demo.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品表
 */
@Data
public class Product {

    private Integer id;
    private Integer categoryId;
    private String name;
    private String enName;
    private String description;
    private String detail;
    private String mainImage;
    private String images;
    private BigDecimal basePrice;
    private BigDecimal originPrice;
    private Integer inventory;
    private Integer salesCount;
    private Integer isHot;
    private Integer isNew;
    private Integer isRecommend;
    private String customOptions;
    private Integer status;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}


