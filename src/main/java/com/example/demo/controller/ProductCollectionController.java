package com.example.demo.controller;

import com.example.demo.http.HttpResult;
import com.example.demo.service.ProductCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商品收藏控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/collection")
@RequiredArgsConstructor
public class ProductCollectionController {

    private final ProductCollectionService productCollectionService;

    /**
     * 添加商品收藏
     * @param productId 商品ID
     * @return 收藏结果
     */
    @PostMapping("/add")
    public HttpResult addCollection(@RequestParam Long productId) {
        try {
            // 获取当前用户ID
            Integer userId = getCurrentUserId();
            if (userId == null) {
                return HttpResult.error("用户未登录");
            }

            // 添加收藏
            var collection = productCollectionService.addCollection(userId, productId);
            log.info("用户{}添加商品收藏成功，商品ID：{}", userId, productId);
            return HttpResult.ok("收藏成功", collection);
        } catch (Exception e) {
            log.error("添加收藏失败：", e);
            return HttpResult.error("添加收藏失败：" + e.getMessage());
        }
    }

    /**
     * 取消商品收藏
     * @param productId 商品ID
     * @return 取消结果
     */
    @PostMapping("/remove")
    public HttpResult removeCollection(@RequestParam Long productId) {
        try {
            // 获取当前用户ID
            Integer userId = getCurrentUserId();
            if (userId == null) {
                return HttpResult.error("用户未登录");
            }

            // 取消收藏
            boolean success = productCollectionService.removeCollection(userId, productId);
            if (success) {
                log.info("用户{}取消商品收藏成功，商品ID：{}", userId, productId);
                return HttpResult.ok("取消收藏成功");
            } else {
                return HttpResult.error("取消收藏失败");
            }
        } catch (Exception e) {
            log.error("取消收藏失败：", e);
            return HttpResult.error("取消收藏失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户收藏商品列表
     * @param page 页码
     * @param pageSize 每页大小
     * @return 收藏商品列表
     */
    @GetMapping("/list")
    public HttpResult getUserCollections(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            // 获取当前用户ID
            Integer userId = getCurrentUserId();
            if (userId == null) {
                return HttpResult.error("用户未登录");
            }

            // 获取收藏列表
            var result = productCollectionService.getUserCollections(userId, page, pageSize);
            log.info("用户{}获取收藏商品列表成功", userId);
            return HttpResult.ok("获取收藏列表成功", result);
        } catch (Exception e) {
            log.error("获取收藏列表失败：", e);
            return HttpResult.error("获取收藏列表失败：" + e.getMessage());
        }
    }

    /**
     * 检查商品是否已收藏
     * @param productId 商品ID
     * @return 是否已收藏
     */
    @GetMapping("/check")
    public HttpResult checkCollectionStatus(@RequestParam Long productId) {
        try {
            // 获取当前用户ID
            Integer userId = getCurrentUserId();
            if (userId == null) {
                return HttpResult.ok("检查收藏状态成功", Map.of("isCollected", false));
            }

            // 检查收藏状态
            boolean isCollected = productCollectionService.isCollected(userId, productId);
            log.info("用户{}检查商品收藏状态，商品ID：{}，状态：{}", userId, productId, isCollected);
            return HttpResult.ok("检查收藏状态成功", Map.of("isCollected", isCollected));
        } catch (Exception e) {
            log.error("检查收藏状态失败：", e);
            return HttpResult.error("检查收藏状态失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户收藏总数
     * @return 收藏总数
     */
    @GetMapping("/count")
    public HttpResult getCollectionCount() {
        try {
            // 获取当前用户ID
            Integer userId = getCurrentUserId();
            if (userId == null) {
                return HttpResult.ok("获取收藏总数成功", Map.of("count", 0));
            }

            // 获取收藏总数
            Integer count = productCollectionService.getCollectionCount(userId);
            log.info("用户{}获取收藏总数成功，数量：{}", userId, count);
            return HttpResult.ok("获取收藏总数成功", Map.of("count", count));
        } catch (Exception e) {
            log.error("获取收藏总数失败：", e);
            return HttpResult.error("获取收藏总数失败：" + e.getMessage());
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
