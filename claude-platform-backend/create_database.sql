-- 创建数据库
CREATE DATABASE IF NOT EXISTS claude_platform 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE claude_platform;

-- 验证数据库创建成功
SELECT 'Database claude_platform created successfully!' as message;