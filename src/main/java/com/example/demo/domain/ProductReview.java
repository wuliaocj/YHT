package com.example.demo.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品评价表
 */
@Data
@TableName("product_review")
public class ProductReview {
    private Integer id;
    private Long productId; // 商品ID
    private Integer userId; // 用户ID
    private Integer orderId; // 订单ID
    private Integer rating; // 评分（1-5星）
    private String content; // 评价内容
    private String images; // 评价图片（多个图片URL用逗号分隔）
    private Integer status; // 状态（1-正常，0-禁用）
    private Integer helpfulCount; // 有用数量
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}
