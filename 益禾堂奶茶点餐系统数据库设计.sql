-- 益禾堂奶茶点餐系统数据库表结构
-- 版本：1.0
-- 创建时间：2024年1月

-- ----------------------------
-- 1. 用户表（小程序用户）
-- ----------------------------
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `openid` varchar(100) NOT NULL COMMENT '微信openid，唯一标识',
  `unionid` varchar(100) DEFAULT NULL COMMENT '微信unionid',
  `nickname` varchar(100) DEFAULT NULL COMMENT '微信昵称',
  `avatar_url` varchar(500) DEFAULT NULL COMMENT '微信头像',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号码',
  `gender` tinyint(1) DEFAULT '0' COMMENT '性别：0-未知，1-男，2-女',
  `province` varchar(50) DEFAULT NULL COMMENT '省份',
  `city` varchar(50) DEFAULT NULL COMMENT '城市',
  `integral` int(11) DEFAULT '0' COMMENT '积分',
  `vip_level` tinyint(2) DEFAULT '0' COMMENT '会员等级：0-普通，1-白银，2-黄金，3-钻石',
  `total_consumption` decimal(10,2) DEFAULT '0.00' COMMENT '累计消费金额',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-禁用，1-正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_openid` (`openid`),
  KEY `idx_phone` (`phone`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 2. 商品分类表
-- ----------------------------
CREATE TABLE `category` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `icon` varchar(200) DEFAULT NULL COMMENT '分类图标',
  `description` varchar(200) DEFAULT NULL COMMENT '分类描述',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序序号',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- ----------------------------
-- 3. 商品表
-- ----------------------------
CREATE TABLE `product` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `category_id` int(11) NOT NULL COMMENT '分类ID',
  `name` varchar(100) NOT NULL COMMENT '商品名称',
  `en_name` varchar(100) DEFAULT NULL COMMENT '英文名称',
  `description` text COMMENT '商品描述',
  `detail` text COMMENT '商品详情',
  `main_image` varchar(500) NOT NULL COMMENT '主图',
  `images` text COMMENT '商品图集（JSON数组）',
  `base_price` decimal(10,2) NOT NULL COMMENT '基础价格',
  `origin_price` decimal(10,2) DEFAULT NULL COMMENT '原价',
  `inventory` int(11) DEFAULT '999' COMMENT '库存（-1表示不限）',
  `sales_count` int(11) DEFAULT '0' COMMENT '销量',
  `is_hot` tinyint(1) DEFAULT '0' COMMENT '是否热销：0-否，1-是',
  `is_new` tinyint(1) DEFAULT '1' COMMENT '是否新品：0-否，1-是',
  `is_recommend` tinyint(1) DEFAULT '0' COMMENT '是否推荐：0-否，1-是',
  `custom_options` text COMMENT '定制选项配置（JSON格式）',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-下架，1-上架',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序序号',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort` (`sort_order`),
  KEY `idx_hot` (`is_hot`),
  KEY `idx_sales` (`sales_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ----------------------------
-- 4. 商品规格选项表
-- ----------------------------
CREATE TABLE `spec_option` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '规格选项ID',
  `product_id` int(11) NOT NULL COMMENT '商品ID',
  `spec_type` varchar(20) NOT NULL COMMENT '规格类型：sweetness-甜度，temperature-温度，topping-加料，size-杯型',
  `spec_name` varchar(50) NOT NULL COMMENT '规格名称',
  `spec_value` varchar(50) NOT NULL COMMENT '规格值',
  `extra_price` decimal(10,2) DEFAULT '0.00' COMMENT '额外价格',
  `is_default` tinyint(1) DEFAULT '0' COMMENT '是否默认：0-否，1-是',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序序号',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_spec_type` (`spec_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品规格选项表';

-- ----------------------------
-- 5. 购物车表
-- ----------------------------
CREATE TABLE `cart` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `product_id` int(11) NOT NULL COMMENT '商品ID',
  `quantity` int(11) NOT NULL DEFAULT '1' COMMENT '商品数量',
  `selected_specs` text COMMENT '已选规格（JSON格式）',
  `total_price` decimal(10,2) DEFAULT '0.00' COMMENT '商品总价',
  `is_selected` tinyint(1) DEFAULT '1' COMMENT '是否选中：0-否，1-是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_product` (`user_id`, `product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- ----------------------------
-- 6. 订单表
-- ----------------------------
CREATE TABLE `order` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单号',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `discount_amount` decimal(10,2) DEFAULT '0.00' COMMENT '优惠金额',
  `delivery_fee` decimal(10,2) DEFAULT '0.00' COMMENT '配送费',
  `actual_amount` decimal(10,2) NOT NULL COMMENT '实付金额',
  `payment_method` tinyint(1) DEFAULT '1' COMMENT '支付方式：1-微信支付，2-余额支付',
  `payment_status` tinyint(1) DEFAULT '0' COMMENT '支付状态：0-未支付，1-已支付，2-支付失败',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `transaction_id` varchar(100) DEFAULT NULL COMMENT '微信支付交易号',
  `order_status` tinyint(2) NOT NULL DEFAULT '0' COMMENT '订单状态：0-待付款，1-已付款/制作中，2-制作完成，3-待取餐，4-配送中，5-已完成，6-已取消',
  `order_type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '订单类型：0-到店自取，1-外卖配送',
  `take_code` varchar(10) DEFAULT NULL COMMENT '取餐码',
  `estimated_time` datetime DEFAULT NULL COMMENT '预计取餐/送达时间',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `cancel_reason` varchar(200) DEFAULT NULL COMMENT '取消原因',
  `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
  `user_remark` varchar(200) DEFAULT NULL COMMENT '用户备注',
  `admin_remark` varchar(200) DEFAULT NULL COMMENT '商家备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_payment_status` (`payment_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ----------------------------
-- 7. 订单详情表
-- ----------------------------
CREATE TABLE `order_item` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '订单详情ID',
  `order_id` int(11) NOT NULL COMMENT '订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单号',
  `product_id` int(11) NOT NULL COMMENT '商品ID',
  `product_name` varchar(100) NOT NULL COMMENT '商品名称',
  `product_image` varchar(500) DEFAULT NULL COMMENT '商品图片',
  `spec_info` text COMMENT '规格信息（JSON格式）',
  `unit_price` decimal(10,2) NOT NULL COMMENT '单价',
  `quantity` int(11) NOT NULL DEFAULT '1' COMMENT '数量',
  `total_price` decimal(10,2) NOT NULL COMMENT '总价',
  `rating` tinyint(1) DEFAULT NULL COMMENT '评分（1-5）',
  `review` varchar(500) DEFAULT NULL COMMENT '评价',
  `review_time` datetime DEFAULT NULL COMMENT '评价时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单详情表';

-- ----------------------------
-- 8. 收货地址表
-- ----------------------------
CREATE TABLE `address` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `consignee` varchar(50) NOT NULL COMMENT '收货人姓名',
  `phone` varchar(20) NOT NULL COMMENT '收货人电话',
  `province` varchar(50) NOT NULL COMMENT '省份',
  `city` varchar(50) NOT NULL COMMENT '城市',
  `district` varchar(50) NOT NULL COMMENT '区县',
  `detail_address` varchar(200) NOT NULL COMMENT '详细地址',
  `postal_code` varchar(10) DEFAULT NULL COMMENT '邮政编码',
  `is_default` tinyint(1) DEFAULT '0' COMMENT '是否默认：0-否，1-是',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-删除，1-正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- ----------------------------
-- 9. 优惠券表
-- ----------------------------
CREATE TABLE `coupon` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
  `name` varchar(100) NOT NULL COMMENT '优惠券名称',
  `type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '类型：1-满减券，2-折扣券',
  `value` decimal(10,2) NOT NULL COMMENT '优惠值（满减金额或折扣比例）',
  `min_amount` decimal(10,2) DEFAULT '0.00' COMMENT '最低消费金额',
  `total_count` int(11) NOT NULL COMMENT '发行总量',
  `remaining_count` int(11) NOT NULL COMMENT '剩余数量',
  `limit_per_user` int(11) DEFAULT '1' COMMENT '每人限领张数',
  `validity_type` tinyint(1) DEFAULT '1' COMMENT '有效期类型：1-固定日期，2-领取后生效',
  `start_time` datetime DEFAULT NULL COMMENT '有效期开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '有效期结束时间',
  `valid_days` int(11) DEFAULT NULL COMMENT '有效天数（领取后生效）',
  `applicable_products` text COMMENT '适用商品（JSON数组，空表示全部）',
  `applicable_categories` text COMMENT '适用分类（JSON数组，空表示全部）',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_validity` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- ----------------------------
-- 10. 用户优惠券表
-- ----------------------------
CREATE TABLE `user_coupon` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户优惠券ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `coupon_id` int(11) NOT NULL COMMENT '优惠券ID',
  `coupon_code` varchar(50) DEFAULT NULL COMMENT '优惠券码',
  `status` tinyint(1) DEFAULT '0' COMMENT '状态：0-未使用，1-已使用，2-已过期',
  `used_time` datetime DEFAULT NULL COMMENT '使用时间',
  `used_order_id` int(11) DEFAULT NULL COMMENT '使用的订单ID',
  `receive_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_coupon_id` (`coupon_id`),
  KEY `idx_status` (`status`),
  KEY `idx_expire` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- ----------------------------
-- 11. 促销活动表
-- ----------------------------
CREATE TABLE `promotion` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `name` varchar(100) NOT NULL COMMENT '活动名称',
  `type` tinyint(1) NOT NULL COMMENT '活动类型：1-满减，2-折扣，3-特价，4-套餐',
  `description` text COMMENT '活动描述',
  `rule_config` text NOT NULL COMMENT '活动规则配置（JSON格式）',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序序号',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_status_time` (`status`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='促销活动表';

-- ----------------------------
-- 12. 管理员表
-- ----------------------------
CREATE TABLE `admin` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码（加密）',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像',
  `role` tinyint(1) DEFAULT '1' COMMENT '角色：1-普通管理员，2-店长，3-超级管理员',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- ----------------------------
-- 13. 系统配置表
-- ----------------------------
CREATE TABLE `config` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text COMMENT '配置值',
  `config_name` varchar(100) DEFAULT NULL COMMENT '配置名称',
  `config_group` varchar(50) DEFAULT NULL COMMENT '配置分组',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- ----------------------------
-- 14. 轮播图表
-- ----------------------------
CREATE TABLE `banner` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '轮播图ID',
  `title` varchar(100) DEFAULT NULL COMMENT '标题',
  `image_url` varchar(500) NOT NULL COMMENT '图片地址',
  `link_type` tinyint(1) DEFAULT '1' COMMENT '链接类型：1-商品，2-分类，3-网页',
  `link_value` varchar(200) DEFAULT NULL COMMENT '链接值',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序序号',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `start_time` datetime DEFAULT NULL COMMENT '开始展示时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束展示时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- ----------------------------
-- 15. 用户反馈表
-- ----------------------------
CREATE TABLE `feedback` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `user_id` int(11) DEFAULT NULL COMMENT '用户ID',
  `type` tinyint(1) DEFAULT '1' COMMENT '反馈类型：1-产品建议，2-问题反馈，3-投诉',
  `content` text NOT NULL COMMENT '反馈内容',
  `contact` varchar(100) DEFAULT NULL COMMENT '联系方式',
  `images` text COMMENT '图片（JSON数组）',
  `status` tinyint(1) DEFAULT '0' COMMENT '状态：0-未处理，1-已处理',
  `reply` text COMMENT '回复内容',
  `reply_time` datetime DEFAULT NULL COMMENT '回复时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户反馈表';

-- ----------------------------
-- 16. 操作日志表
-- ----------------------------
CREATE TABLE `operation_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `admin_id` int(11) DEFAULT NULL COMMENT '管理员ID',
  `module` varchar(50) DEFAULT NULL COMMENT '操作模块',
  `operation` varchar(100) DEFAULT NULL COMMENT '操作内容',
  `method` varchar(10) DEFAULT NULL COMMENT '请求方法',
  `params` text COMMENT '请求参数',
  `ip` varchar(50) DEFAULT NULL COMMENT '操作IP',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理',
  `execute_time` int(11) DEFAULT NULL COMMENT '执行时间（毫秒）',
  `status` tinyint(1) DEFAULT '1' COMMENT '操作状态：0-失败，1-成功',
  `error_message` text COMMENT '错误信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ----------------------------
-- 插入初始数据
-- ----------------------------

-- 插入管理员初始账号（密码：admin123，使用MD5加密）
INSERT INTO `admin` (`username`, `password`, `real_name`, `role`, `status`) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 3, 1);

-- 插入默认商品分类
INSERT INTO `category` (`name`, `icon`, `description`, `sort_order`, `status`) VALUES
('招牌奶茶', 'icon-naicha', '经典招牌系列', 1, 1),
('鲜果茶饮', 'icon-guocha', '新鲜水果制作', 2, 1),
('芝士奶盖', 'icon-naigai', '香醇奶盖系列', 3, 1),
('季节限定', 'icon-xianliang', '当季特色饮品', 4, 1),
('冰淇淋', 'icon-bingqilin', '冰淇淋系列', 5, 1);

-- 插入默认系统配置
INSERT INTO `config` (`config_key`, `config_value`, `config_name`, `config_group`) VALUES
('shop_name', '益禾堂奶茶店', '店铺名称', 'basic'),
('shop_address', '上海市浦东新区', '店铺地址', 'basic'),
('shop_phone', '400-123-4567', '联系电话', 'basic'),
('business_hours', '08:00-22:00', '营业时间', 'basic'),
('min_order_amount', '20.00', '最低起送价', 'order'),
('delivery_fee', '3.00', '配送费', 'order'),
('delivery_range', '3000', '配送范围(米)', 'order'),
('points_ratio', '10', '积分比例(1元=10积分)', 'member'),
('vip_discount', '0.95', '会员折扣', 'member'),
('wechat_share_title', '益禾堂奶茶，好喝不贵！', '微信分享标题', 'wechat');
