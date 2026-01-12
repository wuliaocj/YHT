package com.example.demo.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置表
 */
@Data
@TableName("Config") // 对应数据库分类表
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


