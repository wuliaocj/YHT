package com.example.demo.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单表
 */
@Data
@TableName("`Order`") // 反引号包裹关键字表名
public class Order {
    private Integer id;
    private String orderNo;
    private Integer userId;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal deliveryFee;
    private BigDecimal actualAmount= BigDecimal.valueOf(0.0);
    private Integer paymentMethod;
    private Integer paymentStatus;
    private LocalDateTime paymentTime;
    private String transactionId;
    private Integer orderStatus;
    private Integer orderType;
    private Integer couponId;
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



