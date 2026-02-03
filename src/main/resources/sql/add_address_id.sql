ALTER TABLE `order` ADD COLUMN `address_id` INT DEFAULT NULL COMMENT '地址ID' AFTER `order_type`;
