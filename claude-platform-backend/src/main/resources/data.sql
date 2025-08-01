-- 初始化系统配置数据
INSERT INTO system_config (id, config_key, config_value, config_desc, created_time) 
SELECT * FROM (
    SELECT 'sc001' as id, 'default_token_limit' as config_key, '5000' as config_value, '默认月度Token额度' as config_desc, 1640995200000 as created_time
    UNION ALL
    SELECT 'sc002', 'max_file_size', '52428800', '最大文件大小（字节）50MB', 1640995200000
    UNION ALL
    SELECT 'sc003', 'file_expire_hours', '24', '文件过期时间（小时）', 1640995200000
    UNION ALL
    SELECT 'sc004', 'max_tokens_per_request', '1000', '单次请求最大Token数', 1640995200000
    UNION ALL
    SELECT 'sc005', 'conversation_history_days', '30', '对话历史保留天数', 1640995200000
    UNION ALL
    SELECT 'sc006', 'auto_cleanup_enabled', 'true', '是否启用自动清理', 1640995200000
) AS tmp
WHERE NOT EXISTS (
    SELECT config_key FROM system_config WHERE config_key = tmp.config_key
);

-- 插入默认管理员账号（密码：admin123）
INSERT INTO users (id, username, user_password, real_name, user_role, token_limit, user_status, created_time) 
SELECT * FROM (
    SELECT 'admin001' as id, 'admin' as username, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P8fmEhsZIjj9He' as user_password, '系统管理员' as real_name, 'ROLE_ADMIN' as user_role, 999999 as token_limit, 'ACTIVE' as user_status, 1640995200000 as created_time
) AS tmp
WHERE NOT EXISTS (
    SELECT username FROM users WHERE username = 'admin'
);