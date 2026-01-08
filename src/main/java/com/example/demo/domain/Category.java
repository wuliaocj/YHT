package com.example.demo.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品分类表
 */
@Data
public class Category {

    private Integer id;
    private String name;
    private String icon;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
}


