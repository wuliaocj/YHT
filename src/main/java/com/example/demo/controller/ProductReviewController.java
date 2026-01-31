package com.example.demo.controller;

import com.example.demo.http.HttpResult;
import com.example.demo.service.ProductReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 商品评价控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ProductReviewController {

    private final ProductReviewService productReviewService;

    /**
     * 添加商品评价
     * @param productId 商品ID
     * @param orderId 订单ID
     * @param rating 评分
     * @param content 评价内容
     * @param images 评价图片
     * @return 评价结果
     */
    @PostMapping("/add")
    public HttpResult addReview(
            @RequestParam Long productId,
            @RequestParam Integer orderId,
            @RequestParam Integer rating,
            @RequestParam String content,
            @RequestParam(required = false) String images) {
        try {
            // 获取当前用户ID
            Integer userId = getCurrentUserId();
            if (userId == null) {
                return HttpResult.error("用户未登录");
            }

            // 添加评价
            var review = productReviewService.addReview(productId, userId, orderId, rating, content, images);
            log.info("用户{}添加商品评价成功，商品ID：{}", userId, productId);
            return HttpResult.ok("评价添加成功", review);
        } catch (Exception e) {
            log.error("添加评价失败：", e);
            return HttpResult.error("添加评价失败：" + e.getMessage());
        }
    }

    /**
     * 获取商品评价列表
     * @param productId 商品ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 评价列表
     */
    @GetMapping("/product/list")
    public HttpResult getProductReviews(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            var result = productReviewService.getProductReviews(productId, page, pageSize);
            return HttpResult.ok("获取商品评价成功", result);
        } catch (Exception e) {
            log.error("获取商品评价失败：", e);
            return HttpResult.error("获取商品评价失败：" + e.getMessage());
        }
    }

    /**
     * 获取商品平均评分
     * @param productId 商品ID
     * @return 平均评分
     */
    @GetMapping("/product/rating")
    public HttpResult getProductAverageRating(@RequestParam Long productId) {
        try {
            var rating = productReviewService.getProductAverageRating(productId);
            return HttpResult.ok("获取商品评分成功", rating);
        } catch (Exception e) {
            log.error("获取商品评分失败：", e);
            return HttpResult.error("获取商品评分失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户评价列表
     * @param page 页码
     * @param pageSize 每页大小
     * @return 评价列表
     */
    @GetMapping("/user/list")
    public HttpResult getUserReviews(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            // 获取当前用户ID
            Integer userId = getCurrentUserId();
            if (userId == null) {
                return HttpResult.error("用户未登录");
            }

            var reviews = productReviewService.getUserReviews(userId, page, pageSize);
            return HttpResult.ok("获取用户评价成功", reviews);
        } catch (Exception e) {
            log.error("获取用户评价失败：", e);
            return HttpResult.error("获取用户评价失败：" + e.getMessage());
        }
    }

    /**
     * 检查商品是否已评价
     * @param orderId 订单ID
     * @param productId 商品ID
     * @return 是否已评价
     */
    @GetMapping("/check")
    public HttpResult checkReviewStatus(
            @RequestParam Integer orderId,
            @RequestParam Long productId) {
        try {
            // 获取当前用户ID
            Integer userId = getCurrentUserId();
            if (userId == null) {
                return HttpResult.error("用户未登录");
            }

            boolean hasReviewed = productReviewService.hasReviewed(orderId, productId);
            return HttpResult.ok("检查评价状态成功", Map.of("hasReviewed", hasReviewed));
        } catch (Exception e) {
            log.error("检查评价状态失败：", e);
            return HttpResult.error("检查评价状态失败：" + e.getMessage());
        }
    }

    /**
     * 增加评价有用数量
     * @param reviewId 评价ID
     * @return 操作结果
     */
    @PostMapping("/helpful/{reviewId}")
    public HttpResult incrementHelpfulCount(@PathVariable Integer reviewId) {
        try {
            // 获取当前用户ID
            Integer userId = getCurrentUserId();
            if (userId == null) {
                return HttpResult.error("用户未登录");
            }

            boolean success = productReviewService.incrementHelpfulCount(reviewId);
            if (success) {
                return HttpResult.ok("操作成功");
            } else {
                return HttpResult.error("操作失败");
            }
        } catch (Exception e) {
            log.error("增加评价有用数量失败：", e);
            return HttpResult.error("增加评价有用数量失败：" + e.getMessage());
        }
    }

    /**
     * 获取当前登录用户ID
     */
    private Integer getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            // 这里假设使用的是用户ID作为认证主体
            // 实际实现可能需要根据具体的认证方式进行调整
            Object principal = authentication.getPrincipal();
            if (principal instanceof Integer) {
                return (Integer) principal;
            } else if (principal instanceof String) {
                // 如果是字符串，可能是用户名或其他标识，需要进一步处理
                // 这里简化处理，实际项目中需要根据具体情况调整
                log.warn("认证主体类型不是Integer，而是：{}", principal.getClass().getName());
                return null;
            }
            return null;
        } catch (Exception e) {
            log.warn("获取当前用户ID失败：", e);
            return null;
        }
    }
}
