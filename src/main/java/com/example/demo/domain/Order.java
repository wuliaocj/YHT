package com.example.demo.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单表
 */
@Data
public class Order {
    private Integer id;
    private String orderNo;
    private Integer userId;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal deliveryFee;
    private BigDecimal actualAmount;
    private Integer paymentMethod;
    private Integer paymentStatus;
    private LocalDateTime paymentTime;
    private String transactionId;
    private Integer orderStatus;
    private Integer orderType;
    private String takeCode;
    private LocalDateTime estimatedTime;
    private LocalDateTime completeTime;
    private String cancelReason;
    private LocalDateTime cancelTime;
    private String userRemark;
    private String adminRemark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}


