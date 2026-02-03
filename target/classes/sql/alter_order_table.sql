-- 修改订单表，添加优惠券ID字段
ALTER TABLE `order`
ADD COLUMN `coupon_id` INT DEFAULT NULL COMMENT '优惠券ID' AFTER `order_type`;
