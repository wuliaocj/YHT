package com.example.demo.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 促销活动表
 */
@Data
public class Promotion {

    private Integer id;
    private String name;
    private Integer type;
    private String description;
    private String ruleConfig;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private Integer sortOrder;
    private LocalDateTime createTime;
}


