package com.example.demo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDTO {
    private Integer productId;
    private String productName;
    private String productImage;
    private String specInfo;
    private BigDecimal unitPrice;
    private Integer quantity;
}
