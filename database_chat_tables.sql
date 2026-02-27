-- 聊天功能数据库表创建脚本
-- 执行此脚本前，请确保数据库已存在 contact_info 字段

-- 1. 创建聊天会话表
CREATE TABLE IF NOT EXISTS chats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL COMMENT '贴文作者（物品所有者）',
    requester_id BIGINT NOT NULL COMMENT '联系者（想要物品的人）',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_item_id (item_id),
    INDEX idx_owner_id (owner_id),
    INDEX idx_requester_id (requester_id),
    INDEX idx_updated_at (updated_at),
    FOREIGN KEY (item_id) REFERENCES lost_found_items(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 创建聊天消息表
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL COMMENT '发送者用户ID',
    content VARCHAR(2000) NOT NULL COMMENT '消息内容',
    is_read BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已读',
    created_at DATETIME NOT NULL,
    INDEX idx_chat_id (chat_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_created_at (created_at),
    INDEX idx_chat_created (chat_id, created_at),
    FOREIGN KEY (chat_id) REFERENCES chats(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 添加 contact_info 字段到 lost_found_items 表（如果还没有）
-- ALTER TABLE lost_found_items ADD COLUMN IF NOT EXISTS contact_info VARCHAR(150) NULL AFTER location;

