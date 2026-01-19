package com.example.demo.config;

// 订单状态枚举
public enum OrderStatusEnum {
    PENDING_PAYMENT(1, "待付款"),
    PAID(2, "已付款"),
    PREPARING(3, "制作中"),
    READY(4, "待取餐/待配送"),
    COMPLETED(5, "已完成"),
    CANCELLED(6, "已取消");

    private final Integer code;
    private final String desc;

    OrderStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }
}

