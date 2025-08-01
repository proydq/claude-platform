package com.proshine.claudeplatformbackend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 启动横幅和重要信息提示
 */
@Component
@Order(1000) // 确保在数据初始化之后执行
public class StartupBanner implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(StartupBanner.class);
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    
    @Override
    public void run(String... args) throws Exception {
        printBanner();
    }
    
    private void printBanner() {
        logger.info("");
        logger.info("========================================");
        logger.info("    Claude Platform Backend 启动成功");
        logger.info("========================================");
        logger.info("》 服务端口: {}", serverPort);
        logger.info("》 运行环境: {}", activeProfile);
        logger.info("》 API文档: http://localhost:{}/", serverPort);
        logger.info("》 健康检查: http://localhost:{}/api/health", serverPort);
        logger.info("");
        
        if ("dev".equals(activeProfile)) {
            logger.info("【开发环境提示】");
            logger.info("》 默认管理员账户:");
            logger.info("   用户名: admin");
            logger.info("   密码: 123456");
            logger.info("》 请及时修改默认密码!");
            logger.info("");
        }
        
        if ("prod".equals(activeProfile)) {
            logger.warn("【生产环境提醒】");
            logger.warn("》 请确保已修改默认管理员密码!");
            logger.warn("》 请确保数据库和JWT密钥安全!");
            logger.warn("》 请检查跨域配置是否正确!");
            logger.info("");
        }
        
        logger.info("========================================");
        logger.info("");
    }
}