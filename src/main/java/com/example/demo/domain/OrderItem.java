package com.example.demo.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单详情表
 */
@Data
@TableName("Order_Item")
public class OrderItem {
    private Integer id;
    private Integer orderId;
    private String orderNo;
    private Integer productId;
    private String productName;
    private String productImage;
    private String specInfo;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
    private Integer rating;
    private String review;
    private LocalDateTime reviewTime;
    private LocalDateTime createTime;
}


