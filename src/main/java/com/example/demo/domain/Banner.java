package com.example.demo.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 轮播图表
 */
@Data
public class Banner {

    private Integer id;
    private String title;
    private String imageUrl;
    private Integer linkType;
    private String linkValue;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
}


