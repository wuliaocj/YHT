package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.domain.ProductReview;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.ProductReviewMapper;
import com.example.demo.service.ProductReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品评价服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductReviewServiceImpl extends ServiceImpl<ProductReviewMapper, ProductReview> implements ProductReviewService {

    private final ProductReviewMapper productReviewMapper;

    /**
     * 添加商品评价
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductReview addReview(Long productId, Integer userId, Integer orderId, Integer rating, String content, String images) {
        try {
            // 验证参数
            if (productId == null) {
                throw new BusinessException("商品ID不能为空");
            }
            if (userId == null) {
                throw new BusinessException("用户ID不能为空");
            }
            if (orderId == null) {
                throw new BusinessException("订单ID不能为空");
            }
            if (rating == null || rating < 1 || rating > 5) {
                throw new BusinessException("评分必须在1-5之间");
            }
            if (content == null || content.trim().isEmpty()) {
                throw new BusinessException("评价内容不能为空");
            }

            // 检查是否已评价
            if (hasReviewed(orderId, productId)) {
                throw new BusinessException("该商品已经评价过了");
            }

            // 创建评价
            ProductReview review = new ProductReview();
            review.setProductId(productId);
            review.setUserId(userId);
            review.setOrderId(orderId);
            review.setRating(rating);
            review.setContent(content.trim());
            review.setImages(images);
            review.setStatus(1);
            review.setHelpfulCount(0);
            review.setCreateTime(LocalDateTime.now());
            review.setUpdateTime(LocalDateTime.now());

            // 保存评价
            productReviewMapper.insert(review);
            log.info("添加商品评价成功，评价ID：{}，商品ID：{}，用户ID：{}", review.getId(), productId, userId);

            return review;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("添加商品评价失败：", e);
            throw new BusinessException("添加商品评价失败：" + e.getMessage());
        }
    }

    /**
     * 获取商品评价列表
     */
    @Override
    public Map<String, Object> getProductReviews(Long productId, Integer page, Integer pageSize) {
        try {
            if (productId == null) {
                throw new BusinessException("商品ID不能为空");
            }
            if (page == null || page < 1) {
                page = 1;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 10;
            }

            // 计算偏移量
            Integer offset = (page - 1) * pageSize;

            // 查询评价列表
            List<ProductReview> reviews = productReviewMapper.selectByProductId(productId, offset, pageSize);

            // 查询评价总数
            Integer total = productReviewMapper.countByProductId(productId);

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("reviews", reviews);
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("totalPages", (total + pageSize - 1) / pageSize);

            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取商品评价列表失败：", e);
            throw new BusinessException("获取商品评价列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取商品平均评分
     */
    @Override
    public Double getProductAverageRating(Long productId) {
        try {
            if (productId == null) {
                throw new BusinessException("商品ID不能为空");
            }

            Double averageRating = productReviewMapper.getAverageRatingByProductId(productId);
            return averageRating != null ? averageRating : 0.0;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取商品平均评分失败：", e);
            throw new BusinessException("获取商品平均评分失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户评价列表
     */
    @Override
    public List<ProductReview> getUserReviews(Integer userId, Integer page, Integer pageSize) {
        try {
            if (userId == null) {
                throw new BusinessException("用户ID不能为空");
            }
            if (page == null || page < 1) {
                page = 1;
            }
            if (pageSize == null || pageSize < 1 || pageSize > 100) {
                pageSize = 10;
            }

            // 计算偏移量
            Integer offset = (page - 1) * pageSize;

            // 查询用户评价列表
            return productReviewMapper.selectByUserId(userId, offset, pageSize);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取用户评价列表失败：", e);
            throw new BusinessException("获取用户评价列表失败：" + e.getMessage());
        }
    }

    /**
     * 检查用户是否已评价过商品
     */
    @Override
    public boolean hasReviewed(Integer orderId, Long productId) {
        try {
            ProductReview review = productReviewMapper.selectByOrderIdAndProductId(orderId, productId);
            return review != null;
        } catch (Exception e) {
            log.error("检查评价状态失败：", e);
            throw new BusinessException("检查评价状态失败：" + e.getMessage());
        }
    }

    /**
     * 增加评价有用数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean incrementHelpfulCount(Integer reviewId) {
        try {
            if (reviewId == null) {
                throw new BusinessException("评价ID不能为空");
            }

            // 检查评价是否存在
            ProductReview review = this.getById(reviewId);
            if (review == null) {
                throw new BusinessException("评价不存在");
            }

            // 增加有用数量
            review.setHelpfulCount(review.getHelpfulCount() + 1);
            review.setUpdateTime(LocalDateTime.now());
            this.updateById(review);

            log.info("增加评价有用数量成功，评价ID：{}", reviewId);
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("增加评价有用数量失败：", e);
            throw new BusinessException("增加评价有用数量失败：" + e.getMessage());
        }
    }
}
