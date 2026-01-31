package com.example.demo.service;

import com.example.demo.domain.UserPoint;

import java.math.BigDecimal;
import java.util.List;

public interface UserPointService {

    Integer getUserPointBalance(Integer userId);
    
    List<UserPoint> getUserPointRecords(Integer userId);
    
    boolean addPoint(Integer userId, Integer point, String source, String remark);
    
    boolean usePoint(Integer userId, Integer point, String source, String remark);
    
    boolean addPointByOrder(Integer userId, Integer orderId, BigDecimal orderAmount);
}