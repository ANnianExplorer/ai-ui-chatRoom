-- FenDouChat v2 升级脚本
-- 新增：消息撤回字段、投票表、投票选项表、投票记录表

USE fen_dou_chats;

-- 1. 消息表新增字段
ALTER TABLE chat_message
    ADD COLUMN is_recalled TINYINT DEFAULT 0 COMMENT '是否撤回：0-否，1-是',
    ADD COLUMN scheduled_time VARCHAR(30) DEFAULT NULL COMMENT '定时发送时间',
    ADD COLUMN reply_msg_id INT DEFAULT NULL COMMENT '回复的消息ID';

-- 2. 群投票表
CREATE TABLE IF NOT EXISTS chat_vote (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    chat_id      VARCHAR(100) NOT NULL COMMENT '聊天室ID',
    title        VARCHAR(200) NOT NULL COMMENT '投票标题',
    description  VARCHAR(500) DEFAULT NULL COMMENT '投票描述',
    expire_time  VARCHAR(30) DEFAULT NULL COMMENT '过期时间',
    multi_choice TINYINT DEFAULT 0 COMMENT '是否多选：0-否，1-是',
    anonymous    TINYINT DEFAULT 0 COMMENT '是否匿名：0-否，1-是',
    status       TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_by    INT DEFAULT NULL COMMENT '创建人',
    create_time  VARCHAR(30) DEFAULT NULL COMMENT '创建时间',
    update_by    INT DEFAULT NULL COMMENT '修改人',
    update_time  VARCHAR(30) DEFAULT NULL COMMENT '修改时间',
    is_deleted   TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    INDEX idx_chat_id (chat_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群投票表';

-- 3. 投票选项表
CREATE TABLE IF NOT EXISTS chat_vote_option (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    vote_id    INT NOT NULL COMMENT '投票ID',
    content    VARCHAR(200) NOT NULL COMMENT '选项内容',
    vote_count INT DEFAULT 0 COMMENT '投票数',
    status     TINYINT DEFAULT 1,
    create_by  INT DEFAULT NULL,
    create_time VARCHAR(30) DEFAULT NULL,
    update_by  INT DEFAULT NULL,
    update_time VARCHAR(30) DEFAULT NULL,
    is_deleted TINYINT DEFAULT 0,
    INDEX idx_vote_id (vote_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投票选项表';

-- 4. 投票记录表
CREATE TABLE IF NOT EXISTS chat_vote_record (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    vote_id   INT NOT NULL COMMENT '投票ID',
    option_id INT NOT NULL COMMENT '选项ID',
    user_id   INT NOT NULL COMMENT '投票用户ID',
    status    TINYINT DEFAULT 1,
    create_by INT DEFAULT NULL,
    create_time VARCHAR(30) DEFAULT NULL,
    update_by INT DEFAULT NULL,
    update_time VARCHAR(30) DEFAULT NULL,
    is_deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_vote_option_user (vote_id, option_id, user_id),
    INDEX idx_vote_user (vote_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投票记录表';
