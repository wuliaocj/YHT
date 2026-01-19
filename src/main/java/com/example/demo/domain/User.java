package com.example.demo.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户表（小程序用户）
 */
@Data
@TableName("User")
public class User {
    private Integer id;
    private String openid;
    private String unionid;
    private String nickname;
    private String avatarUrl;
    private String phone;
    private Integer gender;
    private String province;
    private String city;
    private Integer integral;
    private Integer vipLevel;
    private BigDecimal totalConsumption;
    private LocalDateTime lastLoginTime;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}


