package com.example.demo.service;

import java.util.Map;

public interface StatisticsService {

    /**
     * 获取系统概览统计
     * @return 系统概览数据
     */
    Map<String, Object> getOverviewStatistics();

    /**
     * 获取订单统计
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 订单统计数据
     */
    Map<String, Object> getOrderStatistics(String startTime, String endTime);

    /**
     * 获取销售统计
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 销售统计数据
     */
    Map<String, Object> getSalesStatistics(String startTime, String endTime);

    /**
     * 获取用户统计
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 用户统计数据
     */
    Map<String, Object> getUserStatistics(String startTime, String endTime);

    /**
     * 获取产品销售排名
     * @param limit 限制数量
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 产品销售排名数据
     */
    Map<String, Object> getProductSalesRanking(int limit, String startTime, String endTime);
}
