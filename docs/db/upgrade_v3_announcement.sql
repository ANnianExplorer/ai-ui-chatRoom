-- FenDouChat v3 升级脚本：系统公告推送历史表
-- 用于记录管理员的系统公告推送历史

USE fen_dou_chats;

-- 系统公告表122
CREATE TABLE IF NOT EXISTS system_announcement (
    id           INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    title        VARCHAR(100) DEFAULT NULL COMMENT '公告标题',
    content      VARCHAR(1000) NOT NULL COMMENT '公告内容',
    online_count INT DEFAULT 0 COMMENT '推送时在线人数',
    status       TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    create_by    INT DEFAULT NULL COMMENT '创建人（管理员ID）',
    create_time  VARCHAR(30) DEFAULT NULL COMMENT '创建时间',
    update_by    INT DEFAULT NULL COMMENT '修改人',
    update_time  VARCHAR(30) DEFAULT NULL COMMENT '修改时间',
    is_deleted   TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告推送历史表';
