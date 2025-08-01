# 生产环境部署指南

## 环境配置

### 开发环境
```bash
# 使用开发环境配置启动
java -jar -Dspring.profiles.active=dev claude-platform-backend.jar
```

### 生产环境
```bash
# 使用生产环境配置启动
java -jar -Dspring.profiles.active=prod claude-platform-backend.jar
```

## 生产环境必要配置

### 1. 环境变量配置

在生产服务器上设置以下环境变量：

```bash
# 数据库配置
export DB_USERNAME=your_db_username
export DB_PASSWORD=your_secure_db_password

# JWT密钥（必须修改为安全的密钥）
export JWT_SECRET=your-very-secure-jwt-secret-key-for-production-use-at-least-32-characters

# 跨域配置（根据你的前端域名修改）
export CORS_ALLOWED_ORIGINS=https://your-domain.com,https://www.your-domain.com

# SSL配置（如果使用）
export SSL_KEYSTORE_PASSWORD=your_ssl_password
```

### 2. 数据库配置

修改 `application-prod.properties` 中的数据库连接：
```properties
spring.datasource.url=jdbc:mysql://your-db-host:3306/claude_platform?useUnicode=true&characterEncoding=utf8&useSSL=true&serverTimezone=Asia/Shanghai
```

### 3. 跨域配置

**重要：** 生产环境必须配置正确的域名，不能使用 `*` 或 localhost：

```properties
# 正确的生产环境配置示例
app.cors.allowed-origins=https://your-frontend-domain.com,https://www.your-frontend-domain.com

# 错误的配置（安全风险）
# app.cors.allowed-origins=*
# app.cors.allowed-origins=http://localhost:3000
```

### 4. HTTPS配置（推荐）

生产环境建议启用HTTPS：

1. 获取SSL证书
2. 修改 `application-prod.properties`：
```properties
server.ssl.enabled=true
server.ssl.key-store-type=PKCS12
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-alias=tomcat
```

### 5. 反向代理配置（Nginx示例）

```nginx
server {
    listen 443 ssl;
    server_name your-domain.com;
    
    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;
    
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # WebSocket支持
    location /ws {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## 部署检查清单

- [ ] 修改数据库连接配置
- [ ] 设置安全的JWT密钥
- [ ] 配置正确的跨域域名
- [ ] 启用HTTPS（推荐）
- [ ] 配置反向代理
- [ ] 设置防火墙规则
- [ ] 配置日志轮转
- [ ] 设置监控和健康检查

## 启动脚本示例

```bash
#!/bin/bash
# start.sh

# 设置环境变量
export SPRING_PROFILES_ACTIVE=prod
export DB_USERNAME=your_db_user
export DB_PASSWORD=your_db_password
export JWT_SECRET=your-secure-jwt-secret
export CORS_ALLOWED_ORIGINS=https://your-domain.com

# 启动应用
nohup java -jar -Xms512m -Xmx1024m claude-platform-backend.jar > app.log 2>&1 &
```

## 注意事项

1. **安全性**：生产环境绝不能使用默认密码和密钥
2. **跨域**：必须配置具体的域名，不能使用通配符
3. **HTTPS**：强烈建议在生产环境启用HTTPS
4. **监控**：配置应用监控和日志收集系统