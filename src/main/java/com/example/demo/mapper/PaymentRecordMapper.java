package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.domain.PaymentRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {

    /**
     * 根据订单号查询支付记录
     */
    PaymentRecord selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据支付单号查询支付记录
     */
    PaymentRecord selectByPaymentNo(@Param("paymentNo") String paymentNo);

    /**
     * 插入支付记录
     */
    int insert(PaymentRecord paymentRecord);

    /**
     * 更新支付记录
     */
    int update(PaymentRecord paymentRecord);
}
