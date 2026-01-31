package com.example.demo.service;

import com.example.demo.domain.PaymentRecord;

import java.util.List;
import java.util.Map;

/**
 * 支付服务接口
 */
public interface PaymentService {

    /**
     * 创建支付单（生成支付参数）
     * @param orderNo 订单号
     * @param userId 用户ID
     * @return 支付参数（用于前端调起支付）
     */
    Map<String, Object> createPayment(String orderNo, Integer userId);

    /**
     * 处理支付回调（微信/支付宝回调）
     * @param callbackData 回调数据
     * @return 处理结果
     */
    String handlePaymentCallback(Map<String, String> callbackData);

    /**
     * 查询支付状态
     * @param orderNo 订单号
     * @return 支付记录
     */
    PaymentRecord getPaymentByOrderNo(String orderNo);

    /**
     * 模拟支付成功（用于测试）
     * @param orderNo 订单号
     * @return 处理结果
     */
    boolean mockPaymentSuccess(String orderNo);

    /**
     * 申请退款
     * @param orderNo 订单号
     * @param userId 用户ID
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 退款结果
     */
    Map<String, Object> applyRefund(String orderNo, Integer userId, java.math.BigDecimal refundAmount, String refundReason);

    /**
     * 查询退款状态
     * @param refundNo 退款单号
     * @param userId 用户ID
     * @return 退款状态信息
     */
    Map<String, Object> queryRefundStatus(String refundNo, Integer userId);

    /**
     * 查询用户支付记录列表
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 支付记录列表
     */
    List<PaymentRecord> getPaymentRecordsByUserId(Integer userId, Integer page, Integer pageSize);

    /**
     * 根据支付单号查询支付记录
     * @param paymentNo 支付单号
     * @param userId 用户ID
     * @return 支付记录
     */
    PaymentRecord getPaymentByPaymentNo(String paymentNo, Integer userId);
}
