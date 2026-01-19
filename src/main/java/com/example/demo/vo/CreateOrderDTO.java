package com.example.demo.vo;


import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

// DTO类
@Data
public class CreateOrderDTO {
    private Integer userId;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal deliveryFee;
    private BigDecimal actualAmount;
    private Integer paymentMethod;
    private String userRemark;
    private List<OrderItemDTO> items;
    private Integer orderType;
}
