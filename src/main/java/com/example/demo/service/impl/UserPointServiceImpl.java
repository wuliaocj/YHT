package com.example.demo.service.impl;

import com.example.demo.domain.UserPoint;
import com.example.demo.mapper.UserPointMapper;
import com.example.demo.service.UserPointService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserPointServiceImpl implements UserPointService {

    private final UserPointMapper userPointMapper;

    public UserPointServiceImpl(UserPointMapper userPointMapper) {
        this.userPointMapper = userPointMapper;
    }

    @Override
    public Integer getUserPointBalance(Integer userId) {
        return userPointMapper.selectUserPointBalance(userId);
    }

    @Override
    public List<UserPoint> getUserPointRecords(Integer userId) {
        return userPointMapper.selectByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addPoint(Integer userId, Integer point, String source, String remark) {
        if (point <= 0) {
            throw new RuntimeException("积分数量必须大于0");
        }

        // 获取当前余额
        Integer currentBalance = userPointMapper.selectUserPointBalance(userId);
        Integer newBalance = currentBalance + point;

        // 创建积分记录
        UserPoint userPoint = new UserPoint();
        userPoint.setUserId(userId);
        userPoint.setType(1); // 增加积分
        userPoint.setPoint(point);
        userPoint.setBalance(newBalance);
        userPoint.setSource(source);
        userPoint.setRemark(remark);
        userPoint.setCreateTime(LocalDateTime.now());

        // 保存积分记录
        userPointMapper.insert(userPoint);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean usePoint(Integer userId, Integer point, String source, String remark) {
        if (point <= 0) {
            throw new RuntimeException("积分数量必须大于0");
        }

        // 获取当前余额
        Integer currentBalance = userPointMapper.selectUserPointBalance(userId);
        if (currentBalance < point) {
            throw new RuntimeException("积分余额不足");
        }

        Integer newBalance = currentBalance - point;

        // 创建积分记录
        UserPoint userPoint = new UserPoint();
        userPoint.setUserId(userId);
        userPoint.setType(2); // 使用积分
        userPoint.setPoint(point);
        userPoint.setBalance(newBalance);
        userPoint.setSource(source);
        userPoint.setRemark(remark);
        userPoint.setCreateTime(LocalDateTime.now());

        // 保存积分记录
        userPointMapper.insert(userPoint);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addPointByOrder(Integer userId, Integer orderId, BigDecimal orderAmount) {
        // 简单规则：每消费1元获得1积分
        Integer point = orderAmount.intValue();
        if (point <= 0) {
            return true; // 订单金额为0，不增加积分
        }

        return addPoint(userId, point, "订单", "订单号: " + orderId);
    }
}
