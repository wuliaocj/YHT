package com.example.demo.service.impl;

import com.example.demo.config.OrderStatusEnum;
import com.example.demo.config.PaymentStatusEnum;
import com.example.demo.domain.Order;
import com.example.demo.domain.PaymentRecord;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.mapper.PaymentRecordMapper;
import com.example.demo.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 支付服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderMapper orderMapper;
    private final PaymentRecordMapper paymentRecordMapper;

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
     * 注意：实际生产环境需要验证签名，这里简化处理
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
     * 生成支付单号
     */
    private String generatePaymentNo() {
        return "PAY" + System.currentTimeMillis() + new Random().nextInt(1000);
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
            params.put("appId", "wx70e7b0411521d834");
            params.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
            params.put("nonceStr", "mock_nonce_" + System.currentTimeMillis());
            params.put("package", "prepay_id=mock_prepay_" + paymentRecord.getPaymentNo());
            params.put("signType", "RSA");
            params.put("paySign", "mock_sign_" + paymentRecord.getPaymentNo());
        }

        return params;
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
