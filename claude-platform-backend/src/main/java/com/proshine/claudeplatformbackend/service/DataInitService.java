package com.proshine.claudeplatformbackend.service;

import com.proshine.claudeplatformbackend.entity.SystemConfig;
import com.proshine.claudeplatformbackend.entity.User;
import com.proshine.claudeplatformbackend.repository.SystemConfigRepository;
import com.proshine.claudeplatformbackend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;

/**
 * 数据初始化服务
 * 在应用启动时检查并创建必要的初始数据
 */
@Service
public class DataInitService implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SystemConfigRepository systemConfigRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("开始数据初始化检查...");
        
        // 初始化系统管理员用户
        initAdminUser();
        
        // 初始化系统配置
        initSystemConfig();
        
        logger.info("数据初始化检查完成");
    }
    
    /**
     * 初始化系统管理员用户
     */
    private void initAdminUser() {
        try {
            // 检查是否已存在管理员用户
            long adminCount = userRepository.countByUserRole("ROLE_ADMIN");
            
            if (adminCount == 0) {
                logger.info("未发现管理员用户，创建默认管理员账户...");
                
                User admin = new User();
                admin.setId(UUID.randomUUID().toString());
                admin.setUsername("admin");
                admin.setUserPassword(passwordEncoder.encode("123456")); // 默认密码
                admin.setRealName("系统管理员");
                admin.setEmail("admin@claude-platform.com");
                admin.setUserRole("ROLE_ADMIN");
                admin.setTokenLimit(100000); // 管理员给更多token限制
                admin.setUserStatus("ACTIVE");
                admin.setCreatedTime(System.currentTimeMillis());
                admin.setUpdatedTime(System.currentTimeMillis());
                
                userRepository.save(admin);
                
                logger.info("默认管理员账户创建成功:");
                logger.info("  用户名: admin");
                logger.info("  密码: 123456");
                logger.info("  请在首次登录后立即修改密码！");
                
            } else {
                logger.info("管理员用户已存在，跳过创建");
            }
            
        } catch (Exception e) {
            logger.error("创建管理员用户失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 初始化系统配置
     */
    private void initSystemConfig() {
        try {
            // 检查并创建默认系统配置
            String[] configKeys = {
                "DEFAULT_TOKEN_LIMIT",
                "MAX_FILE_SIZE", 
                "FILE_EXPIRE_HOURS",
                "MAX_TOKENS_PER_REQUEST",
                "CONVERSATION_HISTORY_DAYS",
                "AUTO_CLEANUP_ENABLED"
            };
            
            String[] configValues = {
                "5000",      // 默认token限制
                "52428800",  // 50MB文件大小限制
                "24",        // 文件24小时过期
                "4000",      // 单次请求最大token
                "30",        // 对话历史保留30天
                "true"       // 启用自动清理
            };
            
            String[] configDescriptions = {
                "用户默认Token限制",
                "文件上传大小限制(字节)",
                "文件过期时间(小时)",
                "单次请求最大Token数",
                "对话历史保留天数",
                "是否启用自动清理"
            };
            
            for (int i = 0; i < configKeys.length; i++) {
                if (!systemConfigRepository.existsByConfigKey(configKeys[i])) {
                    SystemConfig config = new SystemConfig();
                    config.setId(UUID.randomUUID().toString());
                    config.setConfigKey(configKeys[i]);
                    config.setConfigValue(configValues[i]);
                    config.setConfigDesc(configDescriptions[i]);
                    config.setCreatedTime(System.currentTimeMillis());
                    config.setUpdatedTime(System.currentTimeMillis());
                    
                    systemConfigRepository.save(config);
                    logger.info("创建系统配置: {} = {}", configKeys[i], configValues[i]);
                }
            }
            
        } catch (Exception e) {
            logger.error("初始化系统配置失败: {}", e.getMessage(), e);
        }
    }
}