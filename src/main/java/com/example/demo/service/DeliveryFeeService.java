package com.example.demo.service;

import java.math.BigDecimal;

public interface DeliveryFeeService {
    /**
     * 计算配送费
     * @param distance 配送距离（公里）
     * @param weight 订单重量（kg）
     * @param orderAmount 订单金额
     * @return 配送费
     */
    BigDecimal calculateDeliveryFee(double distance, double weight, BigDecimal orderAmount);

    /**
     * 获取免费配送阈值
     * @return 免费配送阈值
     */
    BigDecimal getFreeDeliveryThreshold();

    /**
     * 更新配送费规则
     * @param baseFee 基础配送费
     * @param distanceFee 每公里额外费用
     * @param weightFee 每公斤额外费用
     * @param freeThreshold 免费配送阈值
     */
    void updateDeliveryFeeRule(BigDecimal baseFee, BigDecimal distanceFee, BigDecimal weightFee, BigDecimal freeThreshold);
}
