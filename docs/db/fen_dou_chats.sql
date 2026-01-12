/*
 Navicat Premium Data Transfer

 Source Server         : 本地
 Source Server Type    : MySQL
 Source Server Version : 80036 (8.0.36)
 Source Host           : localhost:3306
 Source Schema         : fen_dou_chats

 Target Server Type    : MySQL
 Target Server Version : 80036 (8.0.36)
 File Encoding         : 65001

 Date: 12/01/2026 15:37:50
*/

create database fen_dou_chats;
use fen_dou_chats;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for chat_favorite
-- ----------------------------
DROP TABLE IF EXISTS `chat_favorite`;
CREATE TABLE `chat_favorite`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `collector_id` int NOT NULL COMMENT '收藏人ID',
  `collected_id` int NOT NULL COMMENT '被收藏人ID',
  `favorite_from` int NOT NULL COMMENT '收藏来源(1:用户,2:群聊,3ai)',
  `favorite_type` int NOT NULL COMMENT '收藏类型(1:text,2:图片,3:文档)',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人，也就是收藏人',
  `content` int NULL DEFAULT NULL COMMENT '收藏内容,也就是收藏的内容的id，这样就可以联合数据库查看信息了',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int NULL DEFAULT 0 COMMENT '是否删除(0:未删除,1:已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_collector_collected_type_content`(`collector_id` ASC, `collected_id` ASC, `favorite_from` ASC, `content` ASC) USING BTREE,
  INDEX `idx_collected_id`(`collected_id` ASC) USING BTREE,
  INDEX `idx_favorite_from`(`favorite_from` ASC) USING BTREE,
  INDEX `idx_favorite_type`(`favorite_type` ASC) USING BTREE,
  INDEX `idx_content`(`content` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '收藏表' ROW_FORMAT = DYNAMIC;


-- ----------------------------
-- Table structure for chat_file
-- ----------------------------
DROP TABLE IF EXISTS `chat_file`;
CREATE TABLE `chat_file`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始文件名',
  `storage_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '存储文件名',
  `file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件存储路径',
  `file_size` int NOT NULL COMMENT '文件大小(字节)',
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件类型',
  `md5` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件MD5值',
  `status` int NULL DEFAULT 1 COMMENT '状态(0:禁用,1:启用)',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` int NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` int NULL DEFAULT 0 COMMENT '是否删除(0:未删除,1:已删除)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文件表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for chat_friend_request
-- ----------------------------
DROP TABLE IF EXISTS `chat_friend_request`;
CREATE TABLE `chat_friend_request`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sender_id` int NOT NULL COMMENT '发送者ID',
  `receiver_id` int NOT NULL COMMENT '接收者ID',
  `verify_msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '验证消息',
  `status` int NULL DEFAULT 0 COMMENT '状态(0:待处理,1:已同意,2:已拒绝)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sender_receiver`(`sender_id` ASC, `receiver_id` ASC) USING BTREE,
  INDEX `fk_friend_request_receiver`(`receiver_id` ASC) USING BTREE,
  CONSTRAINT `fk_friend_request_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `chat_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_friend_request_sender` FOREIGN KEY (`sender_id`) REFERENCES `chat_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '好友请求表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for chat_group
-- ----------------------------
DROP TABLE IF EXISTS `chat_group`;
CREATE TABLE `chat_group`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '群聊名称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '群聊头像',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `status` int NULL DEFAULT 1 COMMENT '状态(0:禁用,1:启用)',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` int NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` int NULL DEFAULT 0 COMMENT '是否删除(0:未删除,1:已删除)',
  `group_call` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '群聊表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for chat_group_member
-- ----------------------------
DROP TABLE IF EXISTS `chat_group_member`;
CREATE TABLE `chat_group_member`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `group_id` int NOT NULL COMMENT '群聊ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `remark` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `status` int NULL DEFAULT 1 COMMENT '状态(0:禁用,1:启用)',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` int NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` int NULL DEFAULT 0 COMMENT '是否删除(0:未删除,1:已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_group_user`(`group_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_group_member_group` FOREIGN KEY (`group_id`) REFERENCES `chat_group` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_group_member_user` FOREIGN KEY (`user_id`) REFERENCES `chat_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '群成员表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for chat_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `chat_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '聊天ID(单聊:user1_id-user2_id,群聊:group_id)',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '消息内容',
  `content_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容类型(text:文本,image:图片,file:文件)',
  `send_id` int NOT NULL COMMENT '发送者ID',
  `file_id` int NULL DEFAULT NULL COMMENT '文件ID',
  `is_read` int NULL DEFAULT 0 COMMENT '是否已读(0:未读,1:已读)',
  `status` int NULL DEFAULT 1 COMMENT '状态(0:禁用,1:启用)',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` int NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` int NULL DEFAULT 0 COMMENT '是否删除(0:未删除,1:已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_chat_id`(`chat_id` ASC) USING BTREE,
  INDEX `idx_send_id`(`send_id` ASC) USING BTREE,
  INDEX `idx_file_id`(`file_id` ASC) USING BTREE,
  CONSTRAINT `fk_message_file` FOREIGN KEY (`file_id`) REFERENCES `chat_file` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息表' ROW_FORMAT = DYNAMIC;


-- ----------------------------
-- Table structure for chat_user
-- ----------------------------
DROP TABLE IF EXISTS `chat_user`;
CREATE TABLE `chat_user`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名称',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户密码',
  `salt` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '盐值',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `gender` int NULL DEFAULT 0 COMMENT '用户性别(0:未知,1:男,2:女)',
  `birthday` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '出生日期',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户手机号',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户头像',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户邮箱',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户地址',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `status` int NULL DEFAULT 1 COMMENT '状态(0:禁用,1:启用)',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` int NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` int NULL DEFAULT 0 COMMENT '是否删除(0:未删除,1:已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of chat_user
-- ----------------------------
INSERT INTO `chat_user` VALUES (0, '机器人', '6b5f09cae90ec53393d48535ed8d7128', 'robot', '机器人', 0, NULL, '11223344559', 'https://img2.baidu.com/it/u=2473380155,1807338550&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500', '13145215201@love.com', NULL, NULL, 1, NULL, '2026-01-10 19:13:00', NULL, '2026-01-11 23:35:55', 0);
INSERT INTO `chat_user` VALUES (1, 'admin', 'b9d11b3be25f5a1a7dc8ca04cd310b28', 'admin', '管理员', 1, NULL, '19099891211', 'http://127.0.0.1:8090/uploads/028ce071c8f8ee1f7c02c479e2fd780f.png', '13099891212@qq.com', NULL, NULL, 1, NULL, '2026-01-04 12:57:31', NULL, '2026-01-06 14:22:34', 0);

-- ----------------------------
-- Table structure for chat_user_friend
-- ----------------------------
DROP TABLE IF EXISTS `chat_user_friend`;
CREATE TABLE `chat_user_friend`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int NOT NULL COMMENT '用户ID',
  `friend_id` int NOT NULL COMMENT '好友ID',
  `remark` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '好友备注',
  `status` int NULL DEFAULT 1 COMMENT '状态(0:禁用,1:启用)',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` int NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` int NULL DEFAULT 0 COMMENT '是否删除(0:未删除,1:已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_friend`(`user_id` ASC, `friend_id` ASC) USING BTREE,
  INDEX `idx_friend_id`(`friend_id` ASC) USING BTREE,
  CONSTRAINT `fk_user_friend_friend` FOREIGN KEY (`friend_id`) REFERENCES `chat_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_friend_user` FOREIGN KEY (`user_id`) REFERENCES `chat_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户好友表' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;

-- 初始化数据 初始化自增ID
ALTER TABLE chat_favorite AUTO_INCREMENT = 1;
ALTER TABLE chat_file AUTO_INCREMENT = 1;
ALTER TABLE chat_group AUTO_INCREMENT = 1;
ALTER TABLE chat_group_member AUTO_INCREMENT = 1;
ALTER TABLE chat_message AUTO_INCREMENT = 1;
ALTER TABLE chat_user AUTO_INCREMENT = 3;
ALTER TABLE chat_user_friend AUTO_INCREMENT = 1;
ALTER TABLE chat_friend_request AUTO_INCREMENT = 1;
