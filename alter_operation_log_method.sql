-- 修改operation_log表的method字段长度
ALTER TABLE `operation_log` 
MODIFY COLUMN `method` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求方法' AFTER `operation`;