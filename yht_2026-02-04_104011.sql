-- MySQL dump 10.13  Distrib 5.7.21, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: yht
-- ------------------------------------------------------
-- Server version	5.7.21-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `address`
--

DROP TABLE IF EXISTS `address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `address` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `consignee` varchar(50) COLLATE utf8_bin NOT NULL COMMENT '收货人姓名',
  `phone` varchar(20) COLLATE utf8_bin NOT NULL COMMENT '收货人电话',
  `province` varchar(50) COLLATE utf8_bin NOT NULL COMMENT '省份',
  `city` varchar(50) COLLATE utf8_bin NOT NULL COMMENT '城市',
  `district` varchar(50) COLLATE utf8_bin NOT NULL COMMENT '区县',
  `detail_address` varchar(200) COLLATE utf8_bin NOT NULL COMMENT '详细地址',
  `postal_code` varchar(10) COLLATE utf8_bin DEFAULT NULL COMMENT '邮政编码',
  `is_default` tinyint(1) DEFAULT '0' COMMENT '是否默认：0-否，1-是',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-删除，1-正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_is_default` (`is_default`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='收货地址表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `address`
--

/*!40000 ALTER TABLE `address` DISABLE KEYS */;
INSERT INTO `address` VALUES (1,1,'tt','123','广东','深圳','龙华区','民治11栋',NULL,0,1,'2026-02-03 15:07:02','2026-02-03 15:07:02');
/*!40000 ALTER TABLE `address` ENABLE KEYS */;

--
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `admin` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username` varchar(50) COLLATE utf8_bin NOT NULL COMMENT '用户名',
  `password` varchar(255) COLLATE utf8_bin NOT NULL COMMENT '密码（加密）',
  `real_name` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) COLLATE utf8_bin DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(500) COLLATE utf8_bin DEFAULT NULL COMMENT '头像',
  `role` tinyint(1) DEFAULT '1' COMMENT '角色：1-普通管理员，2-店长，3-超级管理员',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '最后登录IP',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='管理员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin`
--

/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
INSERT INTO `admin` VALUES (1,'admin123','admin123',NULL,'123',NULL,NULL,3,1,'2026-02-02 13:46:50',NULL,'2026-01-29 09:33:22','2026-02-02 13:46:49');
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;

--
-- Table structure for table `banner`
--

DROP TABLE IF EXISTS `banner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `banner` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '轮播图ID',
  `title` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '标题',
  `image_url` varchar(500) COLLATE utf8_bin NOT NULL COMMENT '图片地址',
  `link_type` tinyint(1) DEFAULT '1' COMMENT '链接类型：1-商品，2-分类，3-网页',
  `link_value` varchar(200) COLLATE utf8_bin DEFAULT NULL COMMENT '链接值',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序序号',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `start_time` datetime DEFAULT NULL COMMENT '开始展示时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束展示时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`,`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='轮播图表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `banner`
--

/*!40000 ALTER TABLE `banner` DISABLE KEYS */;
INSERT INTO `banner` VALUES (1,'新品上市','http://localhost:8080/upload/images/679a3b5bdd8f4bbeaf13261caa73308f.jpg',1,'1',1,1,'2026-01-01 00:00:00','2026-12-31 23:59:59','2026-01-29 09:39:27'),(2,'促销活动','http://localhost:8080/upload/images/d05f8f37e0b4422bb7bf77db94a79eaf.jpg',3,'promotion',2,1,'2026-01-01 00:00:00','2026-12-31 23:59:59','2026-01-29 09:39:27'),(3,'会员福利','http://localhost:8080/upload/images/5d102ea5ca424a0aa0f1af4b4e027143.jpg',3,'member',3,1,'2026-01-01 00:00:00','2026-12-31 23:59:59','2026-01-29 09:39:27');
/*!40000 ALTER TABLE `banner` ENABLE KEYS */;

--
-- Table structure for table `cart`
--

DROP TABLE IF EXISTS `cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cart` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `product_id` int(11) NOT NULL COMMENT '商品ID',
  `quantity` int(11) NOT NULL DEFAULT '1' COMMENT '商品数量',
  `selected_specs` text COLLATE utf8_bin COMMENT '已选规格（JSON格式）',
  `unit_price` decimal(10,2) DEFAULT NULL COMMENT '商品单价',
  `total_price` decimal(10,2) DEFAULT '0.00' COMMENT '商品总价',
  `is_selected` tinyint(1) DEFAULT '1' COMMENT '是否选中：0-否，1-是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `product_name` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  `product_image` varchar(500) COLLATE utf8_bin DEFAULT NULL,
  `spec_ids` text COLLATE utf8_bin,
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='购物车表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart`
--

/*!40000 ALTER TABLE `cart` DISABLE KEYS */;
/*!40000 ALTER TABLE `cart` ENABLE KEYS */;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `category` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) COLLATE utf8_bin NOT NULL COMMENT '分类名称',
  `icon` varchar(200) COLLATE utf8_bin DEFAULT NULL COMMENT '分类图标',
  `description` varchar(200) COLLATE utf8_bin DEFAULT NULL COMMENT '分类描述',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序序号',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='商品分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,'招牌奶茶','http://localhost:8080/upload/images/0782c1fa1e854d4badbd52fab3eed441.jpg','经典招牌系列',1,1,'2026-01-29 09:37:04'),(2,'鲜果茶饮','icon-guocha','新鲜水果制作',2,1,'2026-01-29 09:37:04'),(3,'芝士奶盖','icon-naigai','香醇奶盖系列',3,1,'2026-01-29 09:37:04'),(4,'季节限定','icon-xianliang','当季特色饮品',4,1,'2026-01-29 09:37:04'),(5,'冰淇淋','icon-bingqilin','冰淇淋系列',5,1,'2026-01-29 09:37:04');
/*!40000 ALTER TABLE `category` ENABLE KEYS */;

--
-- Table structure for table `config`
--

DROP TABLE IF EXISTS `config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `config` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) COLLATE utf8_bin NOT NULL COMMENT '配置键',
  `config_value` text COLLATE utf8_bin COMMENT '配置值',
  `config_name` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '配置名称',
  `config_group` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '配置分组',
  `remark` varchar(200) COLLATE utf8_bin DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='系统配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `config`
--

/*!40000 ALTER TABLE `config` DISABLE KEYS */;
/*!40000 ALTER TABLE `config` ENABLE KEYS */;

--
-- Table structure for table `coupon`
--

DROP TABLE IF EXISTS `coupon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `coupon` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
  `name` varchar(100) COLLATE utf8_bin NOT NULL COMMENT '优惠券名称',
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
  `applicable_products` text COLLATE utf8_bin COMMENT '适用商品（JSON数组，空表示全部）',
  `applicable_categories` text COLLATE utf8_bin COMMENT '适用分类（JSON数组，空表示全部）',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_validity` (`start_time`,`end_time`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='优惠券表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coupon`
--

/*!40000 ALTER TABLE `coupon` DISABLE KEYS */;
INSERT INTO `coupon` VALUES (1,'新人满减券',1,5.00,20.00,1000,1000,1,1,'2026-01-01 00:00:00','2026-12-31 23:59:59',NULL,NULL,NULL,1,'2026-01-29 09:39:03'),(2,'全场折扣券',2,0.90,0.00,500,500,2,1,'2026-01-01 00:00:00','2026-12-31 23:59:59',NULL,NULL,NULL,1,'2026-01-29 09:39:03'),(3,'奶茶满减券',1,3.00,15.00,800,800,3,1,'2026-01-01 00:00:00','2026-12-31 23:59:59',NULL,NULL,NULL,1,'2026-01-29 09:39:03');
/*!40000 ALTER TABLE `coupon` ENABLE KEYS */;

--
-- Table structure for table `feedback`
--

DROP TABLE IF EXISTS `feedback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `feedback` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `user_id` int(11) DEFAULT NULL COMMENT '用户ID',
  `type` tinyint(1) DEFAULT '1' COMMENT '反馈类型：1-产品建议，2-问题反馈，3-投诉',
  `content` text COLLATE utf8_bin NOT NULL COMMENT '反馈内容',
  `contact` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '联系方式',
  `images` text COLLATE utf8_bin COMMENT '图片（JSON数组）',
  `status` tinyint(1) DEFAULT '0' COMMENT '状态：0-未处理，1-已处理',
  `reply` text COLLATE utf8_bin COMMENT '回复内容',
  `reply_time` datetime DEFAULT NULL COMMENT '回复时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='用户反馈表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `feedback`
--

/*!40000 ALTER TABLE `feedback` DISABLE KEYS */;
/*!40000 ALTER TABLE `feedback` ENABLE KEYS */;

--
-- Table structure for table `operation_log`
--

DROP TABLE IF EXISTS `operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `operation_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `admin_id` int(11) DEFAULT NULL COMMENT '管理员ID',
  `module` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '操作模块',
  `operation` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '操作内容',
  `method` varchar(100) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '??????',
  `params` text COLLATE utf8_bin COMMENT '请求参数',
  `ip` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '操作IP',
  `user_agent` varchar(500) COLLATE utf8_bin DEFAULT NULL COMMENT '用户代理',
  `execute_time` int(11) DEFAULT NULL COMMENT '执行时间（毫秒）',
  `status` tinyint(1) DEFAULT '1' COMMENT '操作状态：0-失败，1-成功',
  `error_message` text COLLATE utf8_bin COMMENT '错误信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=383307779 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='操作日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `operation_log`
--

/*!40000 ALTER TABLE `operation_log` DISABLE KEYS */;
INSERT INTO `operation_log` VALUES (-1508249599,1,'登录','登录操作','POST /api/admin/login','{\"username\":\"admin123\"}','127.0.0.1','PostmanRuntime/7.51.1',NULL,1,NULL,'2026-02-02 13:46:50'),(-1319563263,1,'登录','登录操作','POST /api/admin/login','{\"username\":\"admin123\"}','127.0.0.1','PostmanRuntime/7.51.0',NULL,1,NULL,'2026-01-31 14:13:32'),(383307778,1,'登录','登录操作','POST /api/admin/login','{\"username\":\"admin123\"}','127.0.0.1','PostmanRuntime/7.51.0',NULL,1,NULL,'2026-01-31 14:06:01');
/*!40000 ALTER TABLE `operation_log` ENABLE KEYS */;

--
-- Table structure for table `order`
--

DROP TABLE IF EXISTS `order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(50) COLLATE utf8_bin NOT NULL COMMENT '订单号',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `discount_amount` decimal(10,2) DEFAULT '0.00' COMMENT '优惠金额',
  `delivery_fee` decimal(10,2) DEFAULT '0.00' COMMENT '配送费',
  `actual_amount` decimal(10,2) NOT NULL COMMENT '实付金额',
  `payment_method` tinyint(1) DEFAULT '1' COMMENT '支付方式：1-微信支付，2-余额支付',
  `payment_status` tinyint(1) DEFAULT '0' COMMENT '支付状态：0-未支付，1-已支付，2-支付失败',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `transaction_id` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '微信支付交易号',
  `order_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '订单状态：0-待付款，1-已付款/制作中，2-制作完成，3-待取餐，4-配送中，5-已完成，6-已取消',
  `order_type` tinyint(1) NOT NULL DEFAULT '0' COMMENT '订单类型：0-到店自取，1-外卖配送',
  `address_id` int(11) DEFAULT NULL COMMENT '地址ID',
  `take_code` varchar(10) COLLATE utf8_bin DEFAULT NULL COMMENT '取餐码',
  `estimated_time` datetime DEFAULT NULL COMMENT '预计取餐/送达时间',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `cancel_reason` varchar(200) COLLATE utf8_bin DEFAULT NULL COMMENT '取消原因',
  `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
  `user_remark` varchar(200) COLLATE utf8_bin DEFAULT NULL COMMENT '用户备注',
  `admin_remark` varchar(200) COLLATE utf8_bin DEFAULT NULL COMMENT '商家备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_order_no` (`order_no`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_payment_status` (`payment_status`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order`
--

/*!40000 ALTER TABLE `order` DISABLE KEYS */;
INSERT INTO `order` VALUES (1,'ORD409239640998940672',1,24.00,0.00,0.00,24.00,1,0,NULL,NULL,1,1,NULL,NULL,NULL,NULL,NULL,NULL,'',NULL,'2026-02-03 14:52:13','2026-02-03 14:52:13'),(2,'ORD409239887108116480',1,24.00,0.00,0.00,24.00,1,0,NULL,NULL,1,1,NULL,NULL,NULL,NULL,NULL,NULL,'',NULL,'2026-02-03 14:53:12','2026-02-03 14:53:12'),(3,'ORD409240025415290880',1,24.00,0.00,3.00,27.00,1,0,NULL,NULL,1,2,NULL,NULL,NULL,NULL,NULL,NULL,'',NULL,'2026-02-03 14:53:45','2026-02-03 14:53:45'),(4,'ORD409242251445669888',1,48.00,0.00,3.00,51.00,1,0,NULL,NULL,1,2,NULL,NULL,NULL,NULL,NULL,NULL,'',NULL,'2026-02-03 15:02:36','2026-02-03 15:02:36'),(5,'ORD409244138576285696',1,24.00,0.00,3.00,27.00,1,0,NULL,NULL,1,2,NULL,NULL,NULL,NULL,NULL,NULL,'',NULL,'2026-02-03 15:10:06','2026-02-03 15:10:06'),(6,'ORD409267319714353152',2,24.00,0.00,3.00,27.00,1,1,'2026-02-03 16:55:36','MOCK_1770108935752',2,2,NULL,NULL,NULL,NULL,NULL,NULL,'',NULL,'2026-02-03 16:42:12','2026-02-03 16:55:35');
/*!40000 ALTER TABLE `order` ENABLE KEYS */;

--
-- Table structure for table `order_item`
--

DROP TABLE IF EXISTS `order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_item` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '订单详情ID',
  `order_id` int(11) NOT NULL COMMENT '订单ID',
  `order_no` varchar(50) COLLATE utf8_bin NOT NULL COMMENT '订单号',
  `product_id` int(11) NOT NULL COMMENT '商品ID',
  `product_name` varchar(100) COLLATE utf8_bin NOT NULL COMMENT '商品名称',
  `product_image` varchar(500) COLLATE utf8_bin DEFAULT NULL COMMENT '商品图片',
  `spec_info` text COLLATE utf8_bin COMMENT '规格信息（JSON格式）',
  `unit_price` decimal(10,2) NOT NULL COMMENT '单价',
  `quantity` int(11) NOT NULL DEFAULT '1' COMMENT '数量',
  `total_price` decimal(10,2) NOT NULL COMMENT '总价',
  `rating` tinyint(1) DEFAULT NULL COMMENT '评分（1-5）',
  `review` varchar(500) COLLATE utf8_bin DEFAULT NULL COMMENT '评价',
  `review_time` datetime DEFAULT NULL COMMENT '评价时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1717067779 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='订单详情表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_item`
--

/*!40000 ALTER TABLE `order_item` DISABLE KEYS */;
INSERT INTO `order_item` VALUES (-1818685438,6,'ORD409267319714353152',6,'11','http://localhost:8080/upload/images/d32159289a4d4daba3a1da760a322251.jpg','中杯/标准糖/去冰/珍珠+布丁',12.00,2,24.00,NULL,NULL,NULL,'2026-02-03 16:42:13'),(-1751621630,3,'ORD409240025415290880',6,'11','http://localhost:8080/upload/images/d32159289a4d4daba3a1da760a322251.jpg','中杯/标准糖/去冰/珍珠+布丁',12.00,2,24.00,NULL,NULL,NULL,'2026-02-03 14:53:45'),(-392667135,4,'ORD409242251445669888',6,'11','http://localhost:8080/upload/images/d32159289a4d4daba3a1da760a322251.jpg','中杯/标准糖/去冰/珍珠+布丁',12.00,4,48.00,NULL,NULL,NULL,'2026-02-03 15:02:36'),(806903809,1,'ORD409239640998940672',6,'11','http://localhost:8080/upload/images/d32159289a4d4daba3a1da760a322251.jpg','中杯/标准糖/去冰/珍珠+布丁',12.00,2,24.00,NULL,NULL,NULL,'2026-02-03 14:52:14'),(1478041601,5,'ORD409244138576285696',6,'11','http://localhost:8080/upload/images/d32159289a4d4daba3a1da760a322251.jpg','中杯/标准糖/去冰/珍珠+布丁',12.00,2,24.00,NULL,NULL,NULL,'2026-02-03 15:10:06'),(1717067778,2,'ORD409239887108116480',6,'11','http://localhost:8080/upload/images/d32159289a4d4daba3a1da760a322251.jpg','中杯/标准糖/去冰/珍珠+布丁',12.00,2,24.00,NULL,NULL,NULL,'2026-02-03 14:53:12');
/*!40000 ALTER TABLE `order_item` ENABLE KEYS */;

--
-- Table structure for table `payment_record`
--

DROP TABLE IF EXISTS `payment_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `payment_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(50) COLLATE utf8_bin NOT NULL COMMENT '关联订单号',
  `payment_no` varchar(64) COLLATE utf8_bin NOT NULL COMMENT '支付系统生成的支付单号',
  `user_id` int(11) NOT NULL COMMENT '支付用户ID',
  `amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '支付金额（和订单actual_amount一致）',
  `payment_method` tinyint(1) NOT NULL COMMENT '支付方式：1-微信支付，2-支付宝，3-现金',
  `payment_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '支付状态：0-待支付，1-支付成功，2-支付失败，3-已退款',
  `transaction_id` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '微信/支付宝返回的交易单号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '支付单创建时间',
  `pay_time` datetime DEFAULT NULL COMMENT '实际支付时间',
  `callback_time` datetime DEFAULT NULL COMMENT '支付平台回调时间',
  `callback_content` text COLLATE utf8_bin COMMENT '支付平台回调的原始数据（用于对账）',
  `refund_amount` decimal(10,2) DEFAULT '0.00' COMMENT '退款金额',
  `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
  `refund_reason` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '退款原因',
  `refund_no` varchar(64) COLLATE utf8_bin DEFAULT NULL COMMENT '退款单号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_payment_no` (`payment_no`),
  KEY `idx_payment_status` (`payment_status`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='支付记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_record`
--

/*!40000 ALTER TABLE `payment_record` DISABLE KEYS */;
INSERT INTO `payment_record` VALUES (1,'ORD409267319714353152','PAY409269495631187968',2,27.00,1,1,'MOCK_1770108935752','2026-02-03 16:50:52','2026-02-03 16:55:36','2026-02-03 16:55:36',NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `payment_record` ENABLE KEYS */;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `product` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `category_id` int(11) NOT NULL COMMENT '分类ID',
  `name` varchar(100) COLLATE utf8_bin NOT NULL COMMENT '商品名称',
  `en_name` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '英文名称',
  `description` text COLLATE utf8_bin COMMENT '商品描述',
  `detail` text COLLATE utf8_bin COMMENT '商品详情',
  `main_image` varchar(500) COLLATE utf8_bin NOT NULL COMMENT '主图',
  `images` text COLLATE utf8_bin COMMENT '商品图集（JSON数组）',
  `base_price` decimal(10,2) NOT NULL COMMENT '基础价格',
  `origin_price` decimal(10,2) DEFAULT NULL COMMENT '原价',
  `inventory` int(11) DEFAULT '999' COMMENT '库存（-1表示不限）',
  `sales_count` int(11) DEFAULT '0' COMMENT '销量',
  `is_hot` tinyint(1) DEFAULT '0' COMMENT '是否热销：0-否，1-是',
  `is_new` tinyint(1) DEFAULT '1' COMMENT '是否新品：0-否，1-是',
  `is_recommend` tinyint(1) DEFAULT '0' COMMENT '是否推荐：0-否，1-是',
  `custom_options` text COLLATE utf8_bin COMMENT '定制选项配置（JSON格式）',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-下架，1-上架',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序序号',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_hot` (`is_hot`),
  KEY `idx_sales` (`sales_count`),
  KEY `idx_sort` (`sort_order`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES (1,1,'珍珠奶茶',NULL,'经典珍珠奶茶',NULL,'pearl_milk_tea.jpg',NULL,12.00,15.00,999,0,0,1,0,NULL,1,1,'2026-01-29 09:38:43','2026-01-29 09:38:43'),(2,1,'波霸奶茶',NULL,'大颗波霸奶茶',NULL,'boba_milk_tea.jpg',NULL,13.00,16.00,999,0,0,1,0,NULL,1,2,'2026-01-29 09:38:43','2026-01-29 09:38:43'),(3,2,'百香果绿茶',NULL,'新鲜百香果绿茶',NULL,'passion_fruit_tea.jpg',NULL,15.00,18.00,999,0,0,1,0,NULL,1,1,'2026-01-29 09:38:43','2026-01-29 09:38:43'),(4,3,'芝士草莓茶',NULL,'芝士草莓茶',NULL,'cheese_strawberry_tea.jpg',NULL,18.00,22.00,999,0,0,1,0,NULL,1,1,'2026-01-29 09:38:43','2026-01-29 09:38:43'),(5,5,'香草冰淇淋',NULL,'香草冰淇淋',NULL,'vanilla_ice_cream.jpg',NULL,8.00,10.00,999,0,0,1,0,NULL,1,1,'2026-01-29 09:38:43','2026-01-29 09:38:43'),(6,1,'11',NULL,'','','http://localhost:8080/upload/images/d32159289a4d4daba3a1da760a322251.jpg',NULL,12.00,12.00,NULL,NULL,0,0,0,NULL,1,0,'2026-01-29 10:10:44','2026-01-29 10:10:44'),(7,0,'2',NULL,'','','http://localhost:8080/upload/images/0b0d299f3c1446dca8f3c1405da399db.jpg',NULL,0.00,0.00,NULL,NULL,0,0,0,NULL,1,0,'2026-01-29 11:01:34','2026-01-29 11:01:43'),(8,1,'招牌奶茶',NULL,'经典招牌奶茶，搭配Q弹珍珠','精选优质茶叶，搭配新鲜牛奶和Q弹珍珠','https://example.com/images/pearl-milk-tea.jpg',NULL,15.00,20.00,NULL,NULL,1,1,1,NULL,1,1,'2026-02-02 14:14:44','2026-02-02 14:14:44'),(9,1,'招牌奶茶',NULL,'经典招牌奶茶，搭配Q弹珍珠','精选优质茶叶，搭配新鲜牛奶和Q弹珍珠','https://example.com/images/pearl-milk-tea.jpg',NULL,15.00,20.00,NULL,NULL,1,1,1,NULL,1,1,'2026-02-02 14:16:04','2026-02-02 14:16:04'),(10,1,'招牌奶茶',NULL,'经典招牌奶茶，搭配Q弹珍珠','精选优质茶叶，搭配新鲜牛奶和Q弹珍珠','https://example.com/images/pearl-milk-tea.jpg',NULL,15.00,20.00,NULL,NULL,1,1,1,NULL,1,1,'2026-02-02 14:26:25','2026-02-02 14:26:25'),(11,1,'招牌奶茶',NULL,'经典招牌奶茶，搭配Q弹珍珠','精选优质茶叶，搭配新鲜牛奶和Q弹珍珠','https://example.com/images/pearl-milk-tea.jpg',NULL,15.00,20.00,NULL,NULL,1,1,1,NULL,0,1,'2026-02-02 14:27:17','2026-02-03 14:08:51');
/*!40000 ALTER TABLE `product` ENABLE KEYS */;

--
-- Table structure for table `product_spec_price`
--

DROP TABLE IF EXISTS `product_spec_price`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `product_spec_price` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` bigint(20) NOT NULL COMMENT '关联商品表id（对应product.id）',
  `spec_type` varchar(50) COLLATE utf8_bin NOT NULL COMMENT '规格类型（cup_type=杯型，topping=小料）',
  `spec_name` varchar(50) COLLATE utf8_bin NOT NULL COMMENT '规格名称（大杯/珍珠/椰果等）',
  `price_add` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '加价金额（正数加价，负数减价，0不变）',
  `status` int(11) NOT NULL DEFAULT '1' COMMENT '状态：1=可用，0=不可用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_product_spec` (`product_id`,`spec_type`,`spec_name`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11404 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='商品规格加价表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_spec_price`
--

/*!40000 ALTER TABLE `product_spec_price` DISABLE KEYS */;
INSERT INTO `product_spec_price` VALUES (1,6,'cup_type','中杯',0.00,1,'2026-01-29 10:10:45','2026-01-29 10:10:45'),(2,6,'cup_type','大杯',0.00,1,'2026-01-29 10:10:45','2026-01-29 10:10:45'),(3,6,'topping','珍珠',0.00,1,'2026-01-29 10:10:45','2026-01-29 10:10:45'),(4,6,'topping','布丁',0.00,1,'2026-01-29 10:10:45','2026-01-29 10:10:45'),(5,6,'topping','椰果',0.00,1,'2026-01-29 10:10:45','2026-01-29 10:10:45'),(6,6,'taste','标准糖',0.00,1,'2026-01-29 10:10:45','2026-01-29 10:10:45'),(7,6,'taste','少糖',0.00,1,'2026-01-29 10:10:45','2026-01-29 10:10:45'),(8,6,'taste','无糖',0.00,1,'2026-01-29 10:10:45','2026-01-29 10:10:45'),(9,6,'temperature','正常冰',0.00,1,'2026-01-29 10:10:45','2026-01-29 10:10:45'),(10,6,'temperature','少冰',0.00,1,'2026-01-29 10:10:45','2026-01-29 10:10:45'),(11,6,'temperature','去冰',0.00,1,'2026-01-29 10:10:45','2026-01-29 10:10:45'),(12,6,'temperature','热饮',0.00,1,'2026-01-29 10:10:45','2026-01-29 10:10:45'),(22,7,'cup_type','中杯',0.00,1,'2026-01-29 11:01:44','2026-01-29 11:01:44'),(23,7,'cup_type','小杯',3.00,1,'2026-01-29 11:01:44','2026-01-29 11:01:44'),(24,7,'temperature','去冰',0.00,1,'2026-01-29 11:01:44','2026-01-29 11:01:44'),(25,7,'temperature','少冰',0.00,1,'2026-01-29 11:01:44','2026-01-29 11:01:44'),(26,7,'temperature','正常冰',0.00,1,'2026-01-29 11:01:44','2026-01-29 11:01:44'),(27,7,'temperature','热饮',0.00,1,'2026-01-29 11:01:44','2026-01-29 11:01:44'),(28,7,'topping','布丁',3.00,1,'2026-01-29 11:01:44','2026-01-29 11:01:44'),(29,7,'topping','椰果',2.00,1,'2026-01-29 11:01:44','2026-01-29 11:01:44'),(30,7,'topping','珍珠',2.00,1,'2026-01-29 11:01:44','2026-01-29 11:01:44'),(31,8,'cup_type','中杯',0.00,1,'2026-02-02 14:14:45','2026-02-02 14:14:45'),(32,8,'cup_type','大杯',0.00,1,'2026-02-02 14:14:45','2026-02-02 14:14:45'),(33,8,'topping','珍珠',0.00,1,'2026-02-02 14:14:45','2026-02-02 14:14:45'),(34,8,'topping','布丁',0.00,1,'2026-02-02 14:14:45','2026-02-02 14:14:45'),(35,8,'topping','椰果',0.00,1,'2026-02-02 14:14:45','2026-02-02 14:14:45'),(36,8,'taste','标准糖',0.00,1,'2026-02-02 14:14:45','2026-02-02 14:14:45'),(37,8,'taste','少糖',0.00,1,'2026-02-02 14:14:45','2026-02-02 14:14:45'),(38,8,'taste','无糖',0.00,1,'2026-02-02 14:14:45','2026-02-02 14:14:45'),(39,8,'temperature','正常冰',0.00,1,'2026-02-02 14:14:45','2026-02-02 14:14:45'),(40,8,'temperature','少冰',0.00,1,'2026-02-02 14:14:45','2026-02-02 14:14:45'),(41,8,'temperature','去冰',0.00,1,'2026-02-02 14:14:45','2026-02-02 14:14:45'),(42,8,'temperature','热饮',0.00,1,'2026-02-02 14:14:45','2026-02-02 14:14:45'),(43,9,'cup_type','中杯',0.00,1,'2026-02-02 14:16:04','2026-02-02 14:16:04'),(44,9,'cup_type','大杯',0.00,1,'2026-02-02 14:16:05','2026-02-02 14:16:05'),(45,9,'topping','珍珠',0.00,1,'2026-02-02 14:16:05','2026-02-02 14:16:05'),(46,9,'topping','布丁',0.00,1,'2026-02-02 14:16:05','2026-02-02 14:16:05'),(47,9,'topping','椰果',0.00,1,'2026-02-02 14:16:05','2026-02-02 14:16:05'),(48,9,'taste','标准糖',0.00,1,'2026-02-02 14:16:05','2026-02-02 14:16:05'),(49,9,'taste','少糖',0.00,1,'2026-02-02 14:16:05','2026-02-02 14:16:05'),(50,9,'taste','无糖',0.00,1,'2026-02-02 14:16:05','2026-02-02 14:16:05'),(51,9,'temperature','正常冰',0.00,1,'2026-02-02 14:16:05','2026-02-02 14:16:05'),(52,9,'temperature','少冰',0.00,1,'2026-02-02 14:16:05','2026-02-02 14:16:05'),(53,9,'temperature','去冰',0.00,1,'2026-02-02 14:16:05','2026-02-02 14:16:05'),(54,9,'temperature','热饮',0.00,1,'2026-02-02 14:16:05','2026-02-02 14:16:05'),(55,10,'cup_type','中杯',0.00,1,'2026-02-02 14:26:25','2026-02-02 14:26:25'),(56,10,'cup_type','大杯',3.00,1,'2026-02-02 14:26:25','2026-02-02 14:26:25'),(57,10,'topping','珍珠',2.00,1,'2026-02-02 14:26:25','2026-02-02 14:26:25'),(58,10,'topping','布丁',3.00,1,'2026-02-02 14:26:25','2026-02-02 14:26:25'),(59,10,'topping','椰果',2.00,1,'2026-02-02 14:26:25','2026-02-02 14:26:25'),(60,10,'taste','标准糖',0.00,1,'2026-02-02 14:26:25','2026-02-02 14:26:25'),(61,10,'taste','少糖',0.00,1,'2026-02-02 14:26:25','2026-02-02 14:26:25'),(62,10,'taste','无糖',0.00,1,'2026-02-02 14:26:25','2026-02-02 14:26:25'),(63,10,'temperature','正常冰',0.00,1,'2026-02-02 14:26:25','2026-02-02 14:26:25'),(64,10,'temperature','少冰',0.00,1,'2026-02-02 14:26:25','2026-02-02 14:26:25'),(65,10,'temperature','去冰',0.00,1,'2026-02-02 14:26:25','2026-02-02 14:26:25'),(66,10,'temperature','热饮',0.00,1,'2026-02-02 14:26:25','2026-02-02 14:26:25'),(11101,11,'cup_type','中杯',0.00,1,'2026-02-02 14:27:18','2026-02-02 14:27:18'),(11102,11,'cup_type','大杯',3.00,1,'2026-02-02 14:27:18','2026-02-02 14:27:18'),(11201,11,'taste','标准糖',0.00,1,'2026-02-02 14:27:18','2026-02-02 14:27:18'),(11202,11,'taste','少糖',0.00,1,'2026-02-02 14:27:18','2026-02-02 14:27:18'),(11203,11,'taste','无糖',0.00,1,'2026-02-02 14:27:18','2026-02-02 14:27:18'),(11301,11,'temperature','正常冰',0.00,1,'2026-02-02 14:27:18','2026-02-02 14:27:18'),(11302,11,'temperature','少冰',0.00,1,'2026-02-02 14:27:18','2026-02-02 14:27:18'),(11303,11,'temperature','去冰',0.00,1,'2026-02-02 14:27:18','2026-02-02 14:27:18'),(11304,11,'temperature','热饮',0.00,1,'2026-02-02 14:27:18','2026-02-02 14:27:18'),(11401,11,'topping','珍珠',2.00,1,'2026-02-02 14:27:18','2026-02-02 14:27:18'),(11402,11,'topping','布丁',3.00,1,'2026-02-02 14:27:18','2026-02-02 14:27:18'),(11403,11,'topping','椰果',2.00,1,'2026-02-02 14:27:18','2026-02-02 14:27:18');
/*!40000 ALTER TABLE `product_spec_price` ENABLE KEYS */;

--
-- Table structure for table `promotion`
--

DROP TABLE IF EXISTS `promotion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `promotion` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `name` varchar(100) COLLATE utf8_bin NOT NULL COMMENT '活动名称',
  `type` tinyint(1) NOT NULL COMMENT '活动类型：1-满减，2-折扣，3-特价，4-套餐',
  `description` text COLLATE utf8_bin COMMENT '活动描述',
  `rule_config` text COLLATE utf8_bin NOT NULL COMMENT '活动规则配置（JSON格式）',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序序号',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_status_time` (`status`,`start_time`,`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='促销活动表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `promotion`
--

/*!40000 ALTER TABLE `promotion` DISABLE KEYS */;
/*!40000 ALTER TABLE `promotion` ENABLE KEYS */;

--
-- Table structure for table `spec_option`
--

DROP TABLE IF EXISTS `spec_option`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `spec_option` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '规格选项ID',
  `product_id` int(11) NOT NULL COMMENT '商品ID',
  `spec_type` varchar(20) COLLATE utf8_bin NOT NULL COMMENT '规格类型：sweetness-甜度，temperature-温度，topping-加料，size-杯型',
  `spec_name` varchar(50) COLLATE utf8_bin NOT NULL COMMENT '规格名称',
  `spec_value` varchar(50) COLLATE utf8_bin NOT NULL COMMENT '规格值',
  `extra_price` decimal(10,2) DEFAULT '0.00' COMMENT '额外价格',
  `is_default` tinyint(1) DEFAULT '0' COMMENT '是否默认：0-否，1-是',
  `sort_order` int(11) DEFAULT '0' COMMENT '排序序号',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_spec_type` (`spec_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='商品规格选项表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `spec_option`
--

/*!40000 ALTER TABLE `spec_option` DISABLE KEYS */;
/*!40000 ALTER TABLE `spec_option` ENABLE KEYS */;

--
-- Table structure for table `t_user`
--

DROP TABLE IF EXISTS `t_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `openid` varchar(100) COLLATE utf8_bin NOT NULL COMMENT '小程序openid',
  `nickname` varchar(50) COLLATE utf8_bin DEFAULT '' COMMENT '昵称',
  `avatar` varchar(255) COLLATE utf8_bin DEFAULT '' COMMENT '头像',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`) COMMENT 'openid唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_user`
--

/*!40000 ALTER TABLE `t_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_user` ENABLE KEYS */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `openid` varchar(100) COLLATE utf8_bin NOT NULL COMMENT '微信openid，唯一标识',
  `unionid` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '微信unionid',
  `nickname` varchar(100) COLLATE utf8_bin DEFAULT NULL COMMENT '微信昵称',
  `avatar_url` varchar(500) COLLATE utf8_bin DEFAULT NULL COMMENT '微信头像',
  `phone` varchar(20) COLLATE utf8_bin DEFAULT NULL COMMENT '手机号码',
  `gender` tinyint(1) DEFAULT '0' COMMENT '性别：0-未知，1-男，2-女',
  `province` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '省份',
  `city` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '城市',
  `integral` int(11) DEFAULT '0' COMMENT '积分',
  `vip_level` tinyint(4) DEFAULT '0' COMMENT '会员等级：0-普通，1-白银，2-黄金，3-钻石',
  `total_consumption` decimal(10,2) DEFAULT '0.00' COMMENT '累计消费金额',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-禁用，1-正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_openid` (`openid`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'1',NULL,NULL,NULL,NULL,0,NULL,NULL,0,0,0.00,NULL,1,'2026-02-02 13:49:20','2026-02-02 13:49:20'),(2,'test_openid_123456',NULL,'测试用户','https://via.placeholder.com/100',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,'2026-02-03 16:32:46','2026-02-03 16:32:46');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;

--
-- Table structure for table `user_coupon`
--

DROP TABLE IF EXISTS `user_coupon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_coupon` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户优惠券ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `coupon_id` int(11) NOT NULL COMMENT '优惠券ID',
  `coupon_code` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '优惠券码',
  `status` tinyint(1) DEFAULT '0' COMMENT '状态：0-未使用，1-已使用，2-已过期',
  `used_time` datetime DEFAULT NULL COMMENT '使用时间',
  `used_order_id` int(11) DEFAULT NULL COMMENT '使用的订单ID',
  `receive_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  PRIMARY KEY (`id`),
  KEY `idx_coupon_id` (`coupon_id`),
  KEY `idx_expire` (`expire_time`),
  KEY `idx_status` (`status`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='用户优惠券表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_coupon`
--

/*!40000 ALTER TABLE `user_coupon` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_coupon` ENABLE KEYS */;

--
-- Dumping routines for database 'yht'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-04 10:40:15
