package com.example.demo.controller;

import com.example.demo.domain.PaymentRecord;
import com.example.demo.domain.User;
import com.example.demo.exception.BusinessException;
import com.example.demo.http.HttpResult;
import com.example.demo.service.PaymentService;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 支付控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 创建支付单（生成支付参数）
     * @param orderNo 订单号
     * @return 支付参数
     */
    @PostMapping("/create")
    public HttpResult createPayment(@RequestParam String orderNo) {
        // 1. 获取当前用户ID
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }

        // 2. 参数校验
        if (orderNo == null || orderNo.trim().isEmpty()) {
            return HttpResult.error("订单号不能为空");
        }

        try {
            // 3. 创建支付单
            Map<String, Object> paymentParams = paymentService.createPayment(orderNo, userId);
            log.info("用户{}创建支付单成功，orderNo：{}", userId, orderNo);
            return HttpResult.ok(paymentParams);
        } catch (BusinessException e) {
            log.warn("创建支付单失败：{}", e.getMessage());
            return HttpResult.error(e.getMessage());
        } catch (Exception e) {
            log.error("创建支付单异常：", e);
            return HttpResult.error("创建支付单失败，请稍后重试");
        }
    }

    /**
     * 查询支付状态
     * @param orderNo 订单号
     * @return 支付记录
     */
    @GetMapping("/status/{orderNo}")
    public HttpResult getPaymentStatus(@PathVariable String orderNo) {
        // 1. 获取当前用户ID
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }

        // 2. 查询支付记录
        PaymentRecord paymentRecord = paymentService.getPaymentByOrderNo(orderNo);
        if (paymentRecord == null) {
            return HttpResult.error("支付记录不存在");
        }

        // 3. 权限校验：只能查询自己的支付记录
        if (!userId.equals(paymentRecord.getUserId())) {
            return HttpResult.error("无权查询该支付记录");
        }

        return HttpResult.ok(paymentRecord);
    }

    /**
     * 支付回调接口（微信/支付宝回调）
     * 注意：实际生产环境需要验证签名
     */
    @PostMapping("/callback")
    public String handleCallback(@RequestBody Map<String, String> callbackData) {
        try {
            log.info("收到支付回调，数据：{}", callbackData);
            return paymentService.handlePaymentCallback(callbackData);
        } catch (Exception e) {
            log.error("处理支付回调异常：", e);
            return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[系统异常]]></return_msg></xml>";
        }
    }

    /**
     * 模拟支付成功（用于测试）
     * @param orderNo 订单号
     * @return 处理结果
     */
    @PostMapping("/mock/success")
    public HttpResult mockPaymentSuccess(@RequestParam String orderNo) {
        // 1. 获取当前用户ID
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }

        // 2. 参数校验
        if (orderNo == null || orderNo.trim().isEmpty()) {
            return HttpResult.error("订单号不能为空");
        }

        try {
            boolean success = paymentService.mockPaymentSuccess(orderNo);
            if (success) {
                log.info("模拟支付成功，orderNo：{}，userId：{}", orderNo, userId);
                return HttpResult.ok("模拟支付成功");
            } else {
                return HttpResult.error("模拟支付失败");
            }
        } catch (Exception e) {
            log.error("模拟支付异常：", e);
            return HttpResult.error("模拟支付失败，请稍后重试");
        }
    }

    /**
     * 申请退款
     * @param orderNo 订单号
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 退款结果
     */
    @PostMapping("/refund/apply")
    public HttpResult applyRefund(@RequestParam String orderNo, @RequestParam java.math.BigDecimal refundAmount, @RequestParam String refundReason) {
        // 1. 获取当前用户ID
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }

        // 2. 参数校验
        if (orderNo == null || orderNo.trim().isEmpty()) {
            return HttpResult.error("订单号不能为空");
        }
        if (refundAmount == null || refundAmount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return HttpResult.error("退款金额必须大于0");
        }
        if (refundReason == null || refundReason.trim().isEmpty()) {
            return HttpResult.error("退款原因不能为空");
        }

        try {
            // 3. 申请退款
            java.util.Map<String, Object> refundResult = paymentService.applyRefund(orderNo, userId, refundAmount, refundReason);
            log.info("用户{}申请退款成功，orderNo：{}", userId, orderNo);
            return HttpResult.ok(refundResult);
        } catch (com.example.demo.exception.BusinessException e) {
            log.warn("申请退款失败：{}", e.getMessage());
            return HttpResult.error(e.getMessage());
        } catch (Exception e) {
            log.error("申请退款异常：", e);
            return HttpResult.error("申请退款失败，请稍后重试");
        }
    }

    /**
     * 查询退款状态
     * @param refundNo 退款单号
     * @return 退款状态信息
     */
    @GetMapping("/refund/status/{refundNo}")
    public HttpResult queryRefundStatus(@PathVariable String refundNo) {
        // 1. 获取当前用户ID
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }

        // 2. 参数校验
        if (refundNo == null || refundNo.trim().isEmpty()) {
            return HttpResult.error("退款单号不能为空");
        }

        try {
            // 3. 查询退款状态
            java.util.Map<String, Object> refundStatus = paymentService.queryRefundStatus(refundNo, userId);
            return HttpResult.ok(refundStatus);
        } catch (com.example.demo.exception.BusinessException e) {
            log.warn("查询退款状态失败：{}", e.getMessage());
            return HttpResult.error(e.getMessage());
        } catch (Exception e) {
            log.error("查询退款状态异常：", e);
            return HttpResult.error("查询退款状态失败，请稍后重试");
        }
    }

    /**
     * 查询用户支付记录列表
     * @param page 页码
     * @param pageSize 每页大小
     * @return 支付记录列表
     */
    @GetMapping("/records")
    public HttpResult getPaymentRecords(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer pageSize) {
        // 1. 获取当前用户ID
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }

        // 2. 参数校验
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }

        try {
            // 3. 查询支付记录
            java.util.List<com.example.demo.domain.PaymentRecord> paymentRecords = paymentService.getPaymentRecordsByUserId(userId, page, pageSize);
            return HttpResult.ok(paymentRecords);
        } catch (Exception e) {
            log.error("查询支付记录异常：", e);
            return HttpResult.error("查询支付记录失败，请稍后重试");
        }
    }

    /**
     * 根据支付单号查询支付记录详情
     * @param paymentNo 支付单号
     * @return 支付记录详情
     */
    @GetMapping("/detail/{paymentNo}")
    public HttpResult getPaymentDetail(@PathVariable String paymentNo) {
        // 1. 获取当前用户ID
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }

        // 2. 参数校验
        if (paymentNo == null || paymentNo.trim().isEmpty()) {
            return HttpResult.error("支付单号不能为空");
        }

        try {
            // 3. 查询支付记录
            com.example.demo.domain.PaymentRecord paymentRecord = paymentService.getPaymentByPaymentNo(paymentNo, userId);
            return HttpResult.ok(paymentRecord);
        } catch (com.example.demo.exception.BusinessException e) {
            log.warn("查询支付记录失败：{}", e.getMessage());
            return HttpResult.error(e.getMessage());
        } catch (Exception e) {
            log.error("查询支付记录异常：", e);
            return HttpResult.error("查询支付记录失败，请稍后重试");
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
