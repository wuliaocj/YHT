package com.example.demo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product") // 对应数据库product表
public class Product {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id; // 雪花算法生成的ID是Long类型

    @NotNull(message = "分类ID不能为空")
    @TableField("category_id") // 显式指定数据库字段名（可选，驼峰自动转换）
    private Integer categoryId;

    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String enName;
    private String description;
    private String detail;
    private String mainImage="1";
    private String images;

    // 基础价格（对应数据库base_price字段）
    @NotNull(message = "基础价格不能为空")
    @Positive(message = "基础价格必须大于0")
    @TableField("base_price")
    private BigDecimal basePrice;

    // 原价（对应数据库origin_price字段）
    @TableField("origin_price")
    private BigDecimal originPrice;

    private Integer inventory;
    private Integer salesCount;
    private Integer isHot;
    private Integer isNew;
    private Integer isRecommend;
    private String customOptions;
    private Integer status;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Comparable<BigDecimal> getBasePriceAmount() {
        return basePrice;
    }
}
