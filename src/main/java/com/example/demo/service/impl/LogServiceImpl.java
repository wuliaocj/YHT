package com.example.demo.service.impl;

import com.example.demo.domain.OperationLog;
import com.example.demo.mapper.OperationLogMapper;
import com.example.demo.service.LogService;
import com.example.demo.vo.PageRequestVO;
import com.example.demo.vo.PageResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class LogServiceImpl implements LogService {

    private final OperationLogMapper operationLogMapper;

    public LogServiceImpl(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordOperationLog(OperationLog operationLog) {
        operationLog.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(operationLog);
        log.debug("记录操作日志，module：{}，operation：{}", operationLog.getModule(), operationLog.getOperation());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordLoginLog(Integer adminId, String username, String ip, String userAgent, Integer status, String errorMessage) {
        OperationLog operationLog = new OperationLog();
        operationLog.setAdminId(adminId);
        operationLog.setModule("登录");
        operationLog.setOperation("登录操作");
        operationLog.setMethod("POST /api/admin/login");
        operationLog.setParams("{\"username\":\"" + username + "\"}");
        operationLog.setIp(ip);
        operationLog.setUserAgent(userAgent);
        operationLog.setStatus(status);
        operationLog.setErrorMessage(errorMessage);
        operationLog.setCreateTime(LocalDateTime.now());
        
        operationLogMapper.insert(operationLog);
        log.debug("记录登录日志，username：{}，status：{}", username, status);
    }

    @Override
    public PageResponseVO<OperationLog> listOperationLogs(PageRequestVO pageRequest, String module, Integer adminId, String startTime, String endTime) {
        // TODO: 实现分页查询操作日志的逻辑
        // 这里暂时返回空列表，实际需要根据条件查询数据库
        List<OperationLog> logs = operationLogMapper.selectByCondition(module, adminId, startTime, endTime);
        
        PageResponseVO<OperationLog> pageResponse = PageResponseVO.of(logs, (long) logs.size(), pageRequest.getPageNum(), pageRequest.getPageSize());
        
        log.debug("查询操作日志列表，数量：{}", logs.size());
        return pageResponse;
    }

    @Override
    public PageResponseVO<OperationLog> listLoginLogs(PageRequestVO pageRequest, String username, String ip, String startTime, String endTime) {
        // TODO: 实现分页查询登录日志的逻辑
        // 这里暂时返回空列表，实际需要根据条件查询数据库
        List<OperationLog> logs = operationLogMapper.selectLoginLogs(username, ip, startTime, endTime);
        
        PageResponseVO<OperationLog> pageResponse = PageResponseVO.of(logs, (long) logs.size(), pageRequest.getPageNum(), pageRequest.getPageSize());
        
        log.debug("查询登录日志列表，数量：{}", logs.size());
        return pageResponse;
    }
}
