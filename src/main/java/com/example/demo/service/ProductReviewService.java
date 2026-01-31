package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.domain.ProductReview;

import java.util.List;
import java.util.Map;

public interface ProductReviewService extends IService<ProductReview> {

    /**
     * 添加商品评价
     * @param productId 商品ID
     * @param userId 用户ID
     * @param orderId 订单ID
     * @param rating 评分
     * @param content 评价内容
     * @param images 评价图片
     * @return 评价信息
     */
    ProductReview addReview(Long productId, Integer userId, Integer orderId, Integer rating, String content, String images);

    /**
     * 获取商品评价列表
     * @param productId 商品ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 评价列表和总数
     */
    Map<String, Object> getProductReviews(Long productId, Integer page, Integer pageSize);

    /**
     * 获取商品平均评分
     * @param productId 商品ID
     * @return 平均评分
     */
    Double getProductAverageRating(Long productId);

    /**
     * 获取用户评价列表
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 评价列表
     */
    List<ProductReview> getUserReviews(Integer userId, Integer page, Integer pageSize);

    /**
     * 检查用户是否已评价过商品
     * @param orderId 订单ID
     * @param productId 商品ID
     * @return 是否已评价
     */
    boolean hasReviewed(Integer orderId, Long productId);

    /**
     * 增加评价有用数量
     * @param reviewId 评价ID
     * @return 是否成功
     */
    boolean incrementHelpfulCount(Integer reviewId);
}
