package com.example.demo.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置表
 */
@Data
public class Config {

    private Integer id;
    private String configKey;
    private String configValue;
    private String configName;
    private String configGroup;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}


