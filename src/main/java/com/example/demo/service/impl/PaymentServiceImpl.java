package com.example.demo.service.impl;

import com.example.demo.config.OrderStatusEnum;
import com.example.demo.config.PaymentStatusEnum;
import com.example.demo.domain.Order;
import com.example.demo.domain.PaymentRecord;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.mapper.PaymentRecordMapper;
import com.example.demo.service.PaymentService;
import com.example.demo.util.PaymentSignatureUtil;
import com.example.demo.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderMapper orderMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final PaymentSignatureUtil signatureUtil;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Value("${wx.pay.api-key:}")
    private String wxPayApiKey;

    @Value("${wx.pay.appid:}")
    private String wxPayAppId;

    @Value("${wx.pay.mchid:}")
    private String wxPayMchId;

    @Value("${wx.pay.notify-url:}")
    private String wxPayNotifyUrl;

    @Value("${alipay.app-id:}")
    private String alipayAppId;

    @Value("${alipay.public-key:}")
    private String alipayPublicKey;

    @Value("${alipay.private-key:}")
    private String alipayPrivateKey;

    @Value("${alipay.notify-url:}")
    private String alipayNotifyUrl;

    @Value("${alipay.return-url:}")
    private String alipayReturnUrl;

    /**
     * 创建支付单（生成支付参数）
     * 注意：这里返回模拟的支付参数，实际生产环境需要调用微信/支付宝SDK
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createPayment(String orderNo, Integer userId) {
        // 1. 校验订单
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!userId.equals(order.getUserId())) {
            throw new BusinessException("无权支付该订单");
        }
        if (!PaymentStatusEnum.UNPAID.getCode().equals(order.getPaymentStatus())) {
            throw new BusinessException("订单已支付或已取消，无需重复支付");
        }
        if (order.getActualAmount() == null || order.getActualAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("支付金额不能为0");
        }

        // 2. 检查是否已有支付单（幂等性：同一订单只能有一个待支付的支付单）
        PaymentRecord existingRecord = paymentRecordMapper.selectByOrderNo(orderNo);
        if (existingRecord != null && PaymentStatusEnum.UNPAID.getCode().equals(existingRecord.getPaymentStatus())) {
            log.info("订单{}已存在待支付记录，返回已有支付单号：{}", orderNo, existingRecord.getPaymentNo());
            return buildPaymentParams(existingRecord);
        }

        // 3. 生成支付单
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setOrderNo(orderNo);
        paymentRecord.setPaymentNo(generatePaymentNo()); // 生成支付单号
        paymentRecord.setUserId(userId);
        paymentRecord.setAmount(order.getActualAmount());
        paymentRecord.setPaymentMethod(order.getPaymentMethod());
        paymentRecord.setPaymentStatus(PaymentStatusEnum.UNPAID.getCode()); // 待支付
        paymentRecord.setCreateTime(LocalDateTime.now());
        paymentRecordMapper.insert(paymentRecord);

        log.info("创建支付单成功，orderNo：{}，paymentNo：{}，amount：{}", orderNo, paymentRecord.getPaymentNo(), paymentRecord.getAmount());

        // 4. 构建支付参数（实际环境需要调用微信/支付宝SDK）
        return buildPaymentParams(paymentRecord);
    }

    /**
     * 处理支付回调（微信/支付宝回调）
     * 增加签名验证，确保回调数据的安全性
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handlePaymentCallback(Map<String, String> callbackData) {
        String paymentNo = callbackData.get("out_trade_no"); // 支付单号
        String transactionId = callbackData.get("transaction_id"); // 微信/支付宝交易号
        String tradeState = callbackData.get("trade_state"); // 支付状态

        if (paymentNo == null) {
            log.error("支付回调失败：支付单号为空");
            return buildCallbackResponse(false, "支付单号为空");
        }

        // 验证签名（安全关键）
        if (!verifyCallbackSignature(callbackData)) {
            log.error("支付回调签名验证失败，可能存在伪造回调，paymentNo：{}", paymentNo);
            return buildCallbackResponse(false, "签名验证失败");
        }

        // 1. 查询支付记录
        PaymentRecord paymentRecord = paymentRecordMapper.selectByPaymentNo(paymentNo);
        if (paymentRecord == null) {
            log.error("支付回调失败：支付记录不存在，paymentNo：{}", paymentNo);
            return buildCallbackResponse(false, "支付记录不存在");
        }

        // 2. 防止重复回调（幂等性检查）
        if (PaymentStatusEnum.PAID.getCode().equals(paymentRecord.getPaymentStatus())) {
            log.warn("支付回调：支付记录已处理，paymentNo：{}", paymentNo);
            return buildCallbackResponse(true, "已处理");
        }

        // 3. 处理支付成功
        if ("SUCCESS".equals(tradeState)) {
            LocalDateTime now = LocalDateTime.now();

            // 更新支付记录
            paymentRecord.setPaymentStatus(PaymentStatusEnum.PAID.getCode());
            paymentRecord.setTransactionId(transactionId);
            paymentRecord.setPayTime(now);
            paymentRecord.setCallbackTime(now);
            paymentRecord.setCallbackContent(callbackData.toString()); // 保存回调原始数据
            paymentRecordMapper.update(paymentRecord);

            // 更新订单状态
            Order order = orderMapper.selectByOrderNo(paymentRecord.getOrderNo());
            if (order != null) {
                order.setPaymentStatus(PaymentStatusEnum.PAID.getCode());
                order.setPaymentTime(now);
                order.setTransactionId(transactionId);
                order.setOrderStatus(OrderStatusEnum.PAID.getCode()); // 已付款/制作中
                orderMapper.update(order);
            }

            log.info("支付回调处理成功，paymentNo：{}，orderNo：{}", paymentNo, paymentRecord.getOrderNo());
            return buildCallbackResponse(true, "处理成功");
        } else {
            // 支付失败
            paymentRecord.setPaymentStatus(PaymentStatusEnum.FAILED.getCode());
            paymentRecord.setCallbackTime(LocalDateTime.now());
            paymentRecord.setCallbackContent(callbackData.toString());
            paymentRecordMapper.update(paymentRecord);

            log.warn("支付回调：支付失败，paymentNo：{}，tradeState：{}", paymentNo, tradeState);
            return buildCallbackResponse(false, "支付失败");
        }
    }

    /**
     * 查询支付状态
     */
    @Override
    public PaymentRecord getPaymentByOrderNo(String orderNo) {
        return paymentRecordMapper.selectByOrderNo(orderNo);
    }

    /**
     * 模拟支付成功（用于测试）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean mockPaymentSuccess(String orderNo) {
        PaymentRecord paymentRecord = paymentRecordMapper.selectByOrderNo(orderNo);
        if (paymentRecord == null) {
            log.error("模拟支付失败：支付记录不存在，orderNo：{}", orderNo);
            return false;
        }

        if (PaymentStatusEnum.PAID.getCode().equals(paymentRecord.getPaymentStatus())) {
            log.warn("模拟支付：订单已支付，orderNo：{}", orderNo);
            return true;
        }

        LocalDateTime now = LocalDateTime.now();

        // 更新支付记录
        paymentRecord.setPaymentStatus(PaymentStatusEnum.PAID.getCode());
        paymentRecord.setTransactionId("MOCK_" + System.currentTimeMillis());
        paymentRecord.setPayTime(now);
        paymentRecord.setCallbackTime(now);
        paymentRecordMapper.update(paymentRecord);

        // 更新订单状态
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null) {
            order.setPaymentStatus(PaymentStatusEnum.PAID.getCode());
            order.setPaymentTime(now);
            order.setTransactionId(paymentRecord.getTransactionId());
            order.setOrderStatus(OrderStatusEnum.PAID.getCode());
            orderMapper.update(order);
        }
        log.info("模拟支付成功，orderNo：{}", orderNo);
        return true;
    }

    /**
     * 申请退款
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> applyRefund(String orderNo, Integer userId, java.math.BigDecimal refundAmount, String refundReason) {
        // 1. 校验订单
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new com.example.demo.exception.BusinessException("订单不存在");
        }
        if (!userId.equals(order.getUserId())) {
            throw new com.example.demo.exception.BusinessException("无权操作该订单");
        }

        // 2. 校验支付记录
        PaymentRecord paymentRecord = paymentRecordMapper.selectByOrderNo(orderNo);
        if (paymentRecord == null) {
            throw new com.example.demo.exception.BusinessException("支付记录不存在");
        }
        if (!PaymentStatusEnum.PAID.getCode().equals(paymentRecord.getPaymentStatus())) {
            throw new com.example.demo.exception.BusinessException("订单未支付或已退款，无法申请退款");
        }

        // 3. 校验退款金额
        if (refundAmount == null || refundAmount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new com.example.demo.exception.BusinessException("退款金额必须大于0");
        }
        if (refundAmount.compareTo(paymentRecord.getAmount()) > 0) {
            throw new com.example.demo.exception.BusinessException("退款金额不能超过支付金额");
        }

        // 4. 生成退款单号
        String refundNo = generateRefundNo();

        // 5. 更新支付记录状态为已退款
        paymentRecord.setPaymentStatus(PaymentStatusEnum.REFUNDED.getCode());
        paymentRecord.setRefundNo(refundNo);
        paymentRecord.setRefundAmount(refundAmount);
        paymentRecord.setRefundReason(refundReason);
        paymentRecord.setRefundTime(LocalDateTime.now());
        paymentRecordMapper.update(paymentRecord);

        // 6. 更新订单状态为已退款
        order.setPaymentStatus(PaymentStatusEnum.REFUNDED.getCode());
        order.setOrderStatus(com.example.demo.config.OrderStatusEnum.CANCELLED.getCode());
        // order.setRefundNo(refundNo); // Order类中无setRefundNo方法，已注释
        orderMapper.update(order);

        // 7. 构建退款结果
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("refundNo", refundNo);
        result.put("orderNo", orderNo);
        result.put("refundAmount", refundAmount);
        result.put("refundTime", paymentRecord.getRefundTime());
        result.put("status", "SUCCESS");

        log.info("申请退款成功，orderNo：{}，refundNo：{}，refundAmount：{}", orderNo, refundNo, refundAmount);
        return result;
    }

    /**
     * 查询退款状态
     */
    @Override
    public Map<String, Object> queryRefundStatus(String refundNo, Integer userId) {
        // 1. 查询支付记录
        PaymentRecord paymentRecord = paymentRecordMapper.selectByRefundNo(refundNo);
        if (paymentRecord == null) {
            throw new com.example.demo.exception.BusinessException("退款记录不存在");
        }

        // 2. 权限校验
        if (!userId.equals(paymentRecord.getUserId())) {
            throw new com.example.demo.exception.BusinessException("无权查询该退款记录");
        }

        // 3. 构建退款状态信息
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("refundNo", refundNo);
        result.put("orderNo", paymentRecord.getOrderNo());
        result.put("refundAmount", paymentRecord.getRefundAmount());
        result.put("refundReason", paymentRecord.getRefundReason());
        result.put("refundTime", paymentRecord.getRefundTime());
        result.put("status", PaymentStatusEnum.REFUNDED.getCode().equals(paymentRecord.getPaymentStatus()) ? "SUCCESS" : "PENDING");

        return result;
    }

    /**
     * 查询用户支付记录列表
     */
    @Override
    public java.util.List<PaymentRecord> getPaymentRecordsByUserId(Integer userId, Integer page, Integer pageSize) {
        if (userId == null) {
            throw new com.example.demo.exception.BusinessException("用户ID不能为空");
        }

        // 计算分页参数
        Integer offset = (page - 1) * pageSize;
        return paymentRecordMapper.selectByUserId(userId, offset, pageSize);
    }

    /**
     * 根据支付单号查询支付记录
     */
    @Override
    public PaymentRecord getPaymentByPaymentNo(String paymentNo, Integer userId) {
        if (paymentNo == null || paymentNo.trim().isEmpty()) {
            throw new com.example.demo.exception.BusinessException("支付单号不能为空");
        }

        PaymentRecord paymentRecord = paymentRecordMapper.selectByPaymentNo(paymentNo);
        if (paymentRecord == null) {
            throw new com.example.demo.exception.BusinessException("支付记录不存在");
        }

        // 权限校验
        if (!userId.equals(paymentRecord.getUserId())) {
            throw new com.example.demo.exception.BusinessException("无权查询该支付记录");
        }

        return paymentRecord;
    }

    /**
     * 生成退款单号
     */
    private String generateRefundNo() {
        return "REFUND_" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    /**
     * 生成支付单号（使用雪花算法）
     */
    private String generatePaymentNo() {
        return snowflakeIdGenerator.generatePaymentNo("PAY");
    }

    /**
     * 构建支付参数（模拟，实际需要调用微信/支付宝SDK）
     */
    private Map<String, Object> buildPaymentParams(PaymentRecord paymentRecord) {
        Map<String, Object> params = new HashMap<>();
        params.put("paymentNo", paymentRecord.getPaymentNo());
        params.put("orderNo", paymentRecord.getOrderNo());
        params.put("amount", paymentRecord.getAmount());
        params.put("paymentMethod", paymentRecord.getPaymentMethod());

        // 模拟微信支付参数（实际需要调用微信SDK）
        if (paymentRecord.getPaymentMethod() == 1) { // 微信支付
            params.put("appId", wxPayAppId != null && !wxPayAppId.isEmpty() ? wxPayAppId : "wx70e7b0411521d834");
            params.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
            params.put("nonceStr", "mock_nonce_" + System.currentTimeMillis());
            params.put("package", "prepay_id=mock_prepay_" + paymentRecord.getPaymentNo());
            params.put("signType", "RSA");
            params.put("paySign", "mock_sign_" + paymentRecord.getPaymentNo());
            params.put("notifyUrl", wxPayNotifyUrl);
        } else if (paymentRecord.getPaymentMethod() == 2) { // 支付宝支付
            params.put("appId", alipayAppId != null && !alipayAppId.isEmpty() ? alipayAppId : "your_alipay_app_id");
            params.put("outTradeNo", paymentRecord.getPaymentNo());
            params.put("subject", "益禾堂奶茶订单" + paymentRecord.getOrderNo());
            params.put("totalAmount", paymentRecord.getAmount().toString());
            params.put("productCode", "FAST_INSTANT_TRADE_PAY");
            params.put("returnUrl", alipayReturnUrl);
            params.put("notifyUrl", alipayNotifyUrl);
        }

        return params;
    }

    /**
     * 验证回调签名
     * @param callbackData 回调数据
     * @return true-签名验证通过，false-签名验证失败
     */
    private boolean verifyCallbackSignature(Map<String, String> callbackData) {
        try {
            // 根据不同的支付方式验证签名
            String payType = callbackData.get("pay_type");
            if ("wx".equals(payType) || callbackData.containsKey("appid")) {
                // 微信支付签名验证
                if (wxPayApiKey == null || wxPayApiKey.isEmpty()) {
                    log.error("微信支付API密钥未配置，无法验证签名");
                    return false;
                }
                return signatureUtil.verifyWxPaySignature(callbackData, wxPayApiKey);
            } else if ("alipay".equals(payType) || callbackData.containsKey("app_id")) {
                // 支付宝签名验证
                if (alipayPublicKey == null || alipayPublicKey.isEmpty()) {
                    log.error("支付宝公钥未配置，无法验证签名");
                    return false;
                }
                return signatureUtil.verifyAlipaySignature(callbackData, alipayPublicKey);
            } else {
                log.warn("未知的支付类型，跳过签名验证：{}", payType);
                return true; // 对于测试环境，可以允许通过
            }
        } catch (Exception e) {
            log.error("签名验证异常", e);
            return false;
        }
    }

    /**
     * 构建回调响应（XML格式，用于微信/支付宝回调）
     */
    private String buildCallbackResponse(boolean success, String message) {
        if (success) {
            return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
        } else {
            return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[" + message + "]]></return_msg></xml>";
        }
    }
}
