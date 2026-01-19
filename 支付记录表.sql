-- 支付记录表
-- 用于记录每一笔支付的详细信息，和订单表一对一关联，方便对账/查错

CREATE TABLE `payment_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(50) NOT NULL COMMENT '关联订单号',
  `payment_no` varchar(64) NOT NULL COMMENT '支付系统生成的支付单号',
  `user_id` int(11) NOT NULL COMMENT '支付用户ID',
  `amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '支付金额（和订单actual_amount一致）',
  `payment_method` tinyint(1) NOT NULL COMMENT '支付方式：1-微信支付，2-支付宝，3-现金',
  `payment_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '支付状态：0-待支付，1-支付成功，2-支付失败，3-已退款',
  `transaction_id` varchar(100) DEFAULT NULL COMMENT '微信/支付宝返回的交易单号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '支付单创建时间',
  `pay_time` datetime DEFAULT NULL COMMENT '实际支付时间',
  `callback_time` datetime DEFAULT NULL COMMENT '支付平台回调时间',
  `callback_content` text COMMENT '支付平台回调的原始数据（用于对账）',
  `refund_amount` decimal(10,2) DEFAULT '0.00' COMMENT '退款金额',
  `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
  `refund_reason` varchar(255) DEFAULT NULL COMMENT '退款原因',
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_payment_no` (`payment_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_payment_status` (`payment_status`),
  UNIQUE KEY `uk_payment_no` (`payment_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';
