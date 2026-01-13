package com.example.demo.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志表
 */
@Data
public class OperationLog {
    private Integer id;
    private Integer adminId;
    private String module;
    private String operation;
    private String method;
    private String params;
    private String ip;
    private String userAgent;
    private Integer executeTime;
    private Integer status;
    private String errorMessage;
    private LocalDateTime createTime;
}


