package com.example.demo.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录表
 */
@Data
@TableName("payment_record")
public class PaymentRecord {
    private Integer id;
    private String orderNo;
    private String paymentNo;
    private Integer userId;
    private BigDecimal amount;
    private Integer paymentMethod; // 支付方式：1-微信支付，2-支付宝，3-现金
    private Integer paymentStatus; // 支付状态：0-待支付，1-支付成功，2-支付失败，3-已退款
    private String transactionId; // 微信/支付宝交易号
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime callbackTime;
    private String callbackContent; // 支付平台回调的原始数据
    private BigDecimal refundAmount; // 退款金额
    private LocalDateTime refundTime;
    private String refundReason; // 退款原因
}
