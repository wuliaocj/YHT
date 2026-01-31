package com.example.demo.service.impl;

import com.example.demo.domain.Order;
import com.example.demo.domain.User;
import com.example.demo.service.OrderService;
import com.example.demo.service.ProductService;
import com.example.demo.service.StatisticsService;
import com.example.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final OrderService orderService;
    private final UserService userService;
    private final ProductService productService;

    public StatisticsServiceImpl(OrderService orderService, UserService userService, ProductService productService) {
        this.orderService = orderService;
        this.userService = userService;
        this.productService = productService;
    }

    @Override
    public Map<String, Object> getOverviewStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        // 获取所有订单
        List<Order> allOrders = orderService.listAllOrders();
        // 获取所有用户
        List<User> allUsers = userService.listAllUsers();
        
        // 计算订单总数
        int totalOrders = allOrders.size();
        
        // 计算总销售额
        double totalSales = allOrders.stream()
                .map(Order::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();
        
        // 计算用户总数
        int totalUsers = allUsers.size();
        
        // 计算今日订单数
        LocalDate today = LocalDate.now();
        int todayOrders = (int) allOrders.stream()
                .filter(order -> order.getCreateTime().toLocalDate().equals(today))
                .count();
        
        // 计算今日销售额
        double todaySales = allOrders.stream()
                .filter(order -> order.getCreateTime().toLocalDate().equals(today))
                .map(Order::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();
        
        result.put("totalOrders", totalOrders);
        result.put("totalSales", totalSales);
        result.put("totalUsers", totalUsers);
        result.put("todayOrders", todayOrders);
        result.put("todaySales", todaySales);
        
        log.debug("获取系统概览统计，订单总数：{}，总销售额：{}，用户总数：{}，今日订单：{}，今日销售：{}", 
                totalOrders, totalSales, totalUsers, todayOrders, todaySales);
        
        return result;
    }

    @Override
    public Map<String, Object> getOrderStatistics(String startTime, String endTime) {
        Map<String, Object> result = new HashMap<>();
        
        List<Order> allOrders = orderService.listAllOrders();
        List<Order> filteredOrders = filterOrdersByTime(allOrders, startTime, endTime);
        
        // 计算订单总数
        int totalOrders = filteredOrders.size();
        
        // 按状态统计订单数
        Map<Integer, Integer> orderStatusCount = new HashMap<>();
        filteredOrders.forEach(order -> {
            orderStatusCount.put(order.getOrderStatus(), orderStatusCount.getOrDefault(order.getOrderStatus(), 0) + 1);
        });
        
        result.put("totalOrders", totalOrders);
        result.put("orderStatusCount", orderStatusCount);
        
        log.debug("获取订单统计，时间范围：{} 至 {}，订单总数：{}", startTime, endTime, totalOrders);
        
        return result;
    }

    @Override
    public Map<String, Object> getSalesStatistics(String startTime, String endTime) {
        Map<String, Object> result = new HashMap<>();
        
        List<Order> allOrders = orderService.listAllOrders();
        List<Order> filteredOrders = filterOrdersByTime(allOrders, startTime, endTime);
        
        // 计算总销售额
        double totalSales = filteredOrders.stream()
                .map(Order::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();
        
        // 按日期统计销售额
        Map<String, Double> dailySales = new HashMap<>();
        filteredOrders.forEach(order -> {
            String date = order.getCreateTime().toLocalDate().toString();
            BigDecimal amount = order.getTotalAmount();
            if (amount != null) {
                double currentAmount = dailySales.getOrDefault(date, 0.0);
                double newAmount = currentAmount + amount.doubleValue();
                dailySales.put(date, newAmount);
            }
        });
        
        result.put("totalSales", totalSales);
        result.put("dailySales", dailySales);
        
        log.debug("获取销售统计，时间范围：{} 至 {}，总销售额：{}", startTime, endTime, totalSales);
        
        return result;
    }

    @Override
    public Map<String, Object> getUserStatistics(String startTime, String endTime) {
        Map<String, Object> result = new HashMap<>();
        
        List<User> allUsers = userService.listAllUsers();
        List<User> filteredUsers = filterUsersByTime(allUsers, startTime, endTime);
        
        // 计算用户总数
        int totalUsers = filteredUsers.size();
        
        // 按日期统计新增用户数
        Map<String, Integer> dailyNewUsers = new HashMap<>();
        filteredUsers.forEach(user -> {
            String date = user.getCreateTime().toLocalDate().toString();
            dailyNewUsers.put(date, dailyNewUsers.getOrDefault(date, 0) + 1);
        });
        
        result.put("totalUsers", totalUsers);
        result.put("dailyNewUsers", dailyNewUsers);
        
        log.debug("获取用户统计，时间范围：{} 至 {}，用户总数：{}", startTime, endTime, totalUsers);
        
        return result;
    }

    @Override
    public Map<String, Object> getProductSalesRanking(int limit, String startTime, String endTime) {
        Map<String, Object> result = new HashMap<>();
        
        // TODO: 实现产品销售排名逻辑
        // 需要从订单详情中统计每个产品的销售数量和金额
        
        log.debug("获取产品销售排名，限制数量：{}，时间范围：{} 至 {}", limit, startTime, endTime);
        
        return result;
    }

    // 辅助方法：根据时间范围过滤订单
    private List<Order> filterOrdersByTime(List<Order> orders, String startTime, String endTime) {
        if (startTime == null && endTime == null) {
            return orders;
        }
        
        final LocalDateTime[] start = new LocalDateTime[1];
        final LocalDateTime[] end = new LocalDateTime[1];
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        if (startTime != null) {
            start[0] = LocalDateTime.parse(startTime, formatter);
        }
        if (endTime != null) {
            end[0] = LocalDateTime.parse(endTime, formatter);
        }
        
        return orders.stream()
                .filter(order -> {
                    LocalDateTime orderTime = order.getCreateTime();
                    if (start[0] != null && orderTime.isBefore(start[0])) {
                        return false;
                    }
                    if (end[0] != null && orderTime.isAfter(end[0])) {
                        return false;
                    }
                    return true;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    // 辅助方法：根据时间范围过滤用户
    private List<User> filterUsersByTime(List<User> users, String startTime, String endTime) {
        if (startTime == null && endTime == null) {
            return users;
        }
        
        final LocalDateTime[] start = new LocalDateTime[1];
        final LocalDateTime[] end = new LocalDateTime[1];
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        if (startTime != null) {
            start[0] = LocalDateTime.parse(startTime, formatter);
        }
        if (endTime != null) {
            end[0] = LocalDateTime.parse(endTime, formatter);
        }
        
        return users.stream()
                .filter(user -> {
                    LocalDateTime createTime = user.getCreateTime();
                    if (start[0] != null && createTime.isBefore(start[0])) {
                        return false;
                    }
                    if (end[0] != null && createTime.isAfter(end[0])) {
                        return false;
                    }
                    return true;
                })
                .collect(java.util.stream.Collectors.toList());
    }
}
