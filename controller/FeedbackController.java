package com.example.demo.controller;

import com.example.demo.domain.Feedback;
import com.example.demo.domain.User;
import com.example.demo.http.HttpResult;
import com.example.demo.service.FeedbackService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final UserService userService;

    /**
     * 获取当前用户的反馈列表
     * @return 反馈列表
     */
    @GetMapping("/list")
    public HttpResult list() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }
        List<Feedback> list = feedbackService.listByUserId(userId);
        log.info("用户{}获取反馈列表成功，反馈数量：{}", userId, list.size());
        return HttpResult.ok(list);
    }

    /**
     * 提交反馈
     * @param feedback 反馈信息
     * @return 提交结果
     */
    @PostMapping("/submit")
    public HttpResult submit(@RequestBody Feedback feedback) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }

        // 参数验证
        if (feedback.getContent() == null || feedback.getContent().trim().isEmpty()) {
            return HttpResult.error("反馈内容不能为空");
        }
        if (feedback.getContent().length() > 500) {
            return HttpResult.error("反馈内容不能超过500字");
        }

        feedback.setUserId(userId);
        Feedback saved = feedbackService.save(feedback);
        log.info("用户{}提交反馈成功，反馈ID：{}", userId, saved.getId());
        return HttpResult.ok(saved);
    }

    /**
     * 删除反馈
     * @param id 反馈ID
     * @return 删除结果
     */
    @PostMapping("/delete/{id}")
    public HttpResult delete(@PathVariable Integer id) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }
        Feedback feedback = feedbackService.getById(id);
        if (feedback == null) {
            return HttpResult.error("反馈不存在");
        }
        if (!userId.equals(feedback.getUserId())) {
            return HttpResult.error("无权删除该反馈");
        }
        feedbackService.delete(id);
        log.info("用户{}删除反馈成功，反馈ID：{}", userId, id);
        return HttpResult.ok("删除成功");
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
