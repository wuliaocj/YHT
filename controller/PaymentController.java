package com.example.demo.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.example.demo.domain.Order;
import com.example.demo.domain.PaymentRecord;
import com.example.demo.domain.User;
import com.example.demo.exception.BusinessException;
import com.example.demo.http.HttpResult;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.service.AlipayService;
import com.example.demo.service.PaymentService;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 支付控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    // 签名类型（与创建订单时一致）
    private static final String SIGN_TYPE = "RSA2";
    // 字符编码
    private static final String CHARSET = "UTF-8";

    private final PaymentService paymentService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private  final AlipayService alipayService;
    private final OrderMapper orderMapper;

    // 支付宝公钥（从配置文件注入）
    @Value("${alipay.public-key:}")
    private String alipayPublicKey;

    public PaymentController(PaymentService paymentService, UserService userService,
                           JwtUtil jwtUtil, AlipayService alipayService, OrderMapper orderMapper) {
        this.paymentService = paymentService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.alipayService = alipayService;
        this.orderMapper = orderMapper;
    }

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
     * 发起支付（微信/支付宝）
     * @param requestData 支付请求数据
     * @return 支付参数
     */
    @PostMapping("/pay")
    public HttpResult payOrder(@RequestBody Map<String, Object> requestData) {
        // 1. 获取当前用户ID
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }

        // 2. 参数校验
        Integer orderId = (Integer) requestData.get("orderId");
        Integer paymentMethod = (Integer) requestData.get("paymentMethod");

        if (orderId == null) {
            return HttpResult.error("订单ID不能为空");
        }
        if (paymentMethod == null || (paymentMethod != 1 && paymentMethod != 2)) {
            return HttpResult.error("支付方式无效");
        }

        try {
            // 3. 查询订单
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                return HttpResult.error("订单不存在");
            }
            if (!userId.equals(order.getUserId())) {
                return HttpResult.error("无权支付该订单");
            }

            // 4. 更新订单的支付方式（如果用户选择了不同的支付方式）
            if (!paymentMethod.equals(order.getPaymentMethod())) {
                order.setPaymentMethod(paymentMethod);
                order.setUpdateTime(java.time.LocalDateTime.now());
                orderMapper.updateById(order);
                log.info("更新订单支付方式，orderId：{}，newPaymentMethod：{}", orderId, paymentMethod);
            }

            // 5. 创建支付单
            Map<String, Object> paymentParams = paymentService.createPayment(order.getOrderNo(), userId);
            log.info("用户{}发起支付，orderId：{}，paymentMethod：{}", userId, orderId, paymentMethod);

            return HttpResult.ok(paymentParams);
        } catch (BusinessException e) {
            log.warn("发起支付失败：{}", e.getMessage());
            return HttpResult.error(e.getMessage());
        } catch (Exception e) {
            log.error("发起支付异常：", e);
            return HttpResult.error("发起支付失败，请稍后重试");
        }
    }

    /**
     * 支付宝扫码支付（生成二维码）
     */
    @PostMapping("/alipayQrCode/{orderId}")
    public ResponseEntity<Map<String, Object>> alipayQrCode(@PathVariable Integer orderId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 1. 获取当前用户ID
            Integer userId = getCurrentUserId();
            if (userId == null) {
                response.put("success", false);
                response.put("message", "用户未登录");
                return ResponseEntity.ok(response);
            }

            // 2. 先创建支付单记录
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                response.put("success", false);
                response.put("message", "订单不存在");
                return ResponseEntity.ok(response);
            }
            String orderNo = order.getOrderNo();

            // 创建支付单（这会创建PaymentRecord）
            Map<String, Object> paymentParams = paymentService.createPayment(orderNo, userId);
            log.info("用户{}创建支付单成功，orderNo：{}", userId, orderNo);

            // 3. 生成支付宝二维码（使用订单号作为out_trade_no）
            String qrCode = alipayService.createQrCodePayment(orderNo);
            if (qrCode != null) {
                response.put("success", true);
                response.put("message", "二维码生成成功");
                response.put("qrCode", qrCode);
            } else {
                response.put("success", false);
                response.put("message", alipayService.getLastErrorMessage());
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("支付宝二维码生成失败", e);
            response.put("success", false);
            response.put("message", "二维码生成失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }


//    /**
//     * 支付宝回调接口
//     * @param params 回调参数
//     *
//     */
//    @PostMapping("/notify")
//    public String alipayNotify(@RequestParam Map<String, String> params) {
//        log.info("收到支付宝回调参数: {}", params);
//        try {
//            boolean success = alipayService.handleNotify(params);
//            return success ? "success" : "fail";
//        } catch (Exception e) {
//            log.error("处理支付宝回调异常", e);
//            return "fail";
//        }
//    }

    /**
     * 支付宝异步通知接收接口
     */
    @PostMapping("/notify")
    public String alipayNotify(HttpServletRequest request) {
        try {
            // 1. 获取支付宝通知的所有参数
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
                String name = iter.next();
                String[] values = requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
                }
                params.put(name, valueStr);
            }

            // 2. 验证签名（核心！防止伪造通知）
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayPublicKey,
                    CHARSET,
                    SIGN_TYPE
            );

            // 标记签名已验证（供Service层参考）
            params.put("_signature_verified", "true");

            // 3. 签名验证通过后，处理业务逻辑
            if (signVerified) {
                // 关键参数提取
                String outTradeNo = params.get("out_trade_no"); // 你的订单号
                String tradeNo = params.get("trade_no"); // 支付宝交易号
                String tradeStatus = params.get("trade_status"); // 交易状态
                String totalAmount = params.get("total_amount"); // 支付金额

                // 4. 判断交易状态（TRADE_SUCCESS 代表支付成功）
                if ("TRADE_SUCCESS".equals(tradeStatus)) {
                    // ========== 核心业务逻辑 ==========
                    // ① 校验订单金额（防止金额篡改）
                    // ② 更新订单状态为「已支付」
                    // ③ 记录支付日志
                    // ④ 触发后续业务（如发货、通知用户等）
                    // =================================

                    log.info("支付宝支付成功！订单号：{}，交易号：{}", outTradeNo, tradeNo);

                    // 调用PaymentService处理支付成功逻辑
                    try {
                        // 传递完整的原始支付宝回调参数（包含sign字段）供Service层验证签名
                        params.put("pay_type", "alipay");
                        alipayService.handleNotify(params);
                        log.info("订单状态更新成功，orderNo：{}", outTradeNo);
                    } catch (Exception e) {
                        log.error("处理支付回调业务逻辑异常：", e);
                        // 返回fail让支付宝重试
                        return "fail";
                    }
                }

                // 5. 必须返回 "success"，告知支付宝通知已处理
                return "success";
            } else {
                // 签名验证失败，记录日志并返回失败
                log.warn("支付宝通知签名验证失败！");
                return "fail";
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            // 异常情况下返回 fail，支付宝会重试
            return "fail";
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
     * @param requestData 请求数据
     * @return 处理结果
     */
    @PostMapping("/mock/success")
    public HttpResult mockPaymentSuccess(@RequestBody Map<String, String> requestData) {
        // 1. 获取当前用户ID
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }

        // 2. 参数校验
        String orderNo = requestData.get("orderNo");
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
