package com.example.demo.service;

import com.example.demo.domain.PaymentRecord;

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
}
