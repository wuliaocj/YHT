package com.example.demo.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品分类表
 */
@Data // 关键：必须加@Data，自动生成getId()/setId()等方法
@TableName("Category") // 对应数据库分类表
public class Category {
    private Integer id;
    private String name;
    private String icon;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
}


