package com.example.demo.service.impl;

import com.example.demo.service.DeliveryFeeService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class DeliveryFeeServiceImpl implements DeliveryFeeService {

    // 基础配送费
    private BigDecimal baseFee = new BigDecimal("3.00");
    
    // 每公里额外费用
    private BigDecimal distanceFee = new BigDecimal("1.00");
    
    // 每公斤额外费用
    private BigDecimal weightFee = new BigDecimal("0.50");
    
    // 免费配送阈值
    private BigDecimal freeDeliveryThreshold = new BigDecimal("30.00");

    @Override
    public BigDecimal calculateDeliveryFee(double distance, double weight, BigDecimal orderAmount) {
        // 暂时固定配送费为3元
        return new BigDecimal("3.00").setScale(2, RoundingMode.HALF_EVEN);
    }

    @Override
    public BigDecimal getFreeDeliveryThreshold() {
        return freeDeliveryThreshold;
    }

    @Override
    public void updateDeliveryFeeRule(BigDecimal baseFee, BigDecimal distanceFee, BigDecimal weightFee, BigDecimal freeThreshold) {
        if (baseFee != null) {
            this.baseFee = baseFee;
        }
        if (distanceFee != null) {
            this.distanceFee = distanceFee;
        }
        if (weightFee != null) {
            this.weightFee = weightFee;
        }
        if (freeThreshold != null) {
            this.freeDeliveryThreshold = freeThreshold;
        }
    }
}
