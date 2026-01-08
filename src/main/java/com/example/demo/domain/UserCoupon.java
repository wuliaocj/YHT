package com.example.demo.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户优惠券表
 */
@Data
public class UserCoupon {

    private Integer id;
    private Integer userId;
    private Integer couponId;
    private String couponCode;
    private Integer status;
    private LocalDateTime usedTime;
    private Integer usedOrderId;
    private LocalDateTime receiveTime;
    private LocalDateTime expireTime;
}


