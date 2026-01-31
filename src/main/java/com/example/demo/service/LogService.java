package com.example.demo.service;

import com.example.demo.domain.OperationLog;
import com.example.demo.vo.PageRequestVO;
import com.example.demo.vo.PageResponseVO;

import java.util.List;

public interface LogService {

    /**
     * 记录操作日志
     * @param log 操作日志信息
     */
    void recordOperationLog(OperationLog log);

    /**
     * 记录登录日志
     * @param adminId 管理员ID
     * @param username 用户名
     * @param ip IP地址
     * @param userAgent 用户代理
     * @param status 登录状态（1-成功，0-失败）
     * @param errorMessage 错误信息
     */
    void recordLoginLog(Integer adminId, String username, String ip, String userAgent, Integer status, String errorMessage);

    /**
     * 查询操作日志列表
     * @param pageRequest 分页请求
     * @param module 模块（可选）
     * @param adminId 管理员ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 分页的操作日志列表
     */
    PageResponseVO<OperationLog> listOperationLogs(PageRequestVO pageRequest, String module, Integer adminId, String startTime, String endTime);

    /**
     * 查询登录日志列表
     * @param pageRequest 分页请求
     * @param username 用户名（可选）
     * @param ip IP地址（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 分页的登录日志列表
     */
    PageResponseVO<OperationLog> listLoginLogs(PageRequestVO pageRequest, String username, String ip, String startTime, String endTime);
}
