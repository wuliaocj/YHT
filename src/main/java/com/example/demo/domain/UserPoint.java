package com.example.demo.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户积分记录表
 */
@Data
public class UserPoint {
    private Integer id;
    private Integer userId;
    private Integer type;
    private Integer point;
    private Integer balance;
    private String source;
    private String remark;
    private LocalDateTime createTime;
}
