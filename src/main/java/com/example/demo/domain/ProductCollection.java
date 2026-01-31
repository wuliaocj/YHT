package com.example.demo.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品收藏表
 */
@Data
@TableName("product_collection")
public class ProductCollection {
    private Integer id;
    private Integer userId; // 用户ID
    private Long productId; // 商品ID
    private LocalDateTime createTime; // 创建时间
}
