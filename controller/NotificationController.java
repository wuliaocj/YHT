package com.example.demo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.annotation.RequiresPermission;
import com.example.demo.domain.Notification;
import com.example.demo.domain.User;
import com.example.demo.http.HttpResult;
import com.example.demo.service.NotificationService;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息通知控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/notification")
@Tag(name = "消息通知")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    /**
     * 获取通知列表
     * @param type 通知类型（SYSTEM/ORDER/PROMOTION）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 通知列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取通知列表")
    public HttpResult getNotificationList(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }

        Page<Notification> page = notificationService.getUserNotifications(userId, type, pageNum, pageSize);
        return HttpResult.ok(page);
    }

    /**
     * 获取未读数量
     * @return 未读数量
     */
    @GetMapping("/unread/count")
    @Operation(summary = "获取未读数量")
    public HttpResult getUnreadCount() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }

        long count = notificationService.getUnreadCount(userId);
        return HttpResult.ok(count);
    }

    /**
     * 获取未读消息统计
     * @return 未读统计
     */
    @GetMapping("/unread/statistics")
    @Operation(summary = "获取未读消息统计")
    public HttpResult getUnreadStatistics() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }

        long total = notificationService.getUnreadCount(userId);
        long orderCount = notificationService.lambdaQuery()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getType, Notification.TYPE_ORDER)
                .eq(Notification::getIsRead, 0)
                .count();
        long systemCount = notificationService.lambdaQuery()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getType, Notification.TYPE_SYSTEM)
                .eq(Notification::getIsRead, 0)
                .count();
        long promotionCount = notificationService.lambdaQuery()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getType, Notification.TYPE_PROMOTION)
                .eq(Notification::getIsRead, 0)
                .count();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total", total);
        statistics.put("orderCount", orderCount);
        statistics.put("systemCount", systemCount);
        statistics.put("promotionCount", promotionCount);

        return HttpResult.ok(statistics);
    }

    /**
     * 标记通知为已读
     * @param notificationId 通知ID
     * @return 操作结果
     */
    @PostMapping("/read/{notificationId}")
    @Operation(summary = "标记通知为已读")
    public HttpResult markAsRead(@PathVariable Integer notificationId) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }

        Notification notification = notificationService.getById(notificationId);
        if (notification == null) {
            return HttpResult.error("通知不存在");
        }

        if (!userId.equals(notification.getUserId())) {
            return HttpResult.error("无权操作该通知");
        }

        boolean success = notificationService.markAsRead(notificationId);
        if (success) {
            return HttpResult.ok("标记成功");
        }
        return HttpResult.error("标记失败");
    }

    /**
     * 标记所有通知为已读
     * @return 操作结果
     */
    @PostMapping("/read/all")
    @Operation(summary = "标记所有通知为已读")
    public HttpResult markAllAsRead() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }

        boolean success = notificationService.markAllAsRead(userId);
        if (success) {
            return HttpResult.ok("标记成功");
        }
        return HttpResult.error("标记失败");
    }

    /**
     * 删除通知
     * @param notificationId 通知ID
     * @return 操作结果
     */
    @PostMapping("/delete/{notificationId}")
    @Operation(summary = "删除通知")
    public HttpResult deleteNotification(@PathVariable Integer notificationId) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }

        Notification notification = notificationService.getById(notificationId);
        if (notification == null) {
            return HttpResult.error("通知不存在");
        }

        if (!userId.equals(notification.getUserId())) {
            return HttpResult.error("无权操作该通知");
        }

        boolean success = notificationService.deleteNotification(notificationId);
        if (success) {
            return HttpResult.ok("删除成功");
        }
        return HttpResult.error("删除失败");
    }

    /**
     * 发送通知给用户（管理员）
     * @param userId 用户ID
     * @param title 标题
     * @param content 内容
     * @return 操作结果
     */
    @PostMapping("/admin/send")
    @Operation(summary = "发送通知给用户")
    @RequiresPermission(code = "notification:manage")
    public HttpResult sendToUser(
            @RequestParam Integer userId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) String image,
            @RequestParam(required = false) String linkType,
            @RequestParam(required = false) String linkId) {
        notificationService.sendToUser(userId, title, content, image, linkType, linkId);
        return HttpResult.ok("发送成功");
    }

    /**
     * 发送通知给所有用户（管理员）
     * @param title 标题
     * @param content 内容
     * @return 操作结果
     */
    @PostMapping("/admin/sendAll")
    @Operation(summary = "发送通知给所有用户")
    @RequiresPermission(code = "notification:manage")
    public HttpResult sendToAll(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) String image) {
        notificationService.sendToAll(title, content, image);
        return HttpResult.ok("发送成功");
    }

    @GetMapping("/admin/list")
    @Operation(summary = "获取所有通知列表（管理员）")
    @RequiresPermission(code = "notification:read")
    public HttpResult getAdminNotificationList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Notification> pageResult = notificationService.getAdminNotifications(type, userId, page, pageSize);
        return HttpResult.ok(pageResult);
    }

    @GetMapping("/admin/statistics")
    @Operation(summary = "获取通知统计（管理员）")
    @RequiresPermission(code = "notification:read")
    public HttpResult getAdminStatistics() {
        Map<String, Object> stats = notificationService.getAdminStatistics();
        return HttpResult.ok(stats);
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

            String openid = (String) authentication.getPrincipal();
            if (openid == null) {
                return null;
            }

            User user = userService.getUserByOpenid(openid);
            return user != null ? user.getId() : null;
        } catch (Exception e) {
            log.warn("获取当前用户ID失败：", e);
            return null;
        }
    }
}
