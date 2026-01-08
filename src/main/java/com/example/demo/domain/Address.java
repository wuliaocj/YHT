package com.example.demo.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收货地址表
 */
@Data
public class Address {

    private Integer id;
    private Integer userId;
    private String consignee;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private String postalCode;
    private Integer isDefault;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}


