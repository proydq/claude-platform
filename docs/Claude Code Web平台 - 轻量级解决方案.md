# Claude Code Web平台 - 轻量级解决方案

## 项目概述

### 目标用户
- 小型团队（10人以内）
- 需要经常使用 Claude Code 处理日志、分析代码
- 希望摆脱命令行的不便

### 核心诉求
- **方便的输入输出**：告别命令行的复制粘贴
- **保存历史记录**：随时查看之前的对话
- **文件管理**：轻松处理日志文件
- **简单可靠**：稳定运行，易于维护

## 系统架构（简化版）

```
浏览器（Web界面）
    ↓
云端服务器（Spring Boot + MySQL）
    ↓
本地客户端（Go开发的小工具）
    ↓
Claude Code（命令行）
```

## 技术选型

### 前端
- **Vue3 + Element Plus**：快速搭建，组件丰富
- **原则**：能用组件就不自己写

### 后端  
- **Spring Boot 2.7.6**：您熟悉，稳定可靠
- **MySQL**：存储用户数据和对话历史
- **原则**：单体应用，不搞微服务

### 本地客户端
- **Go语言**：编译成单个exe文件，用户下载即用
- **原则**：越简单越好

## 功能列表（只做必要的）

### 1. 用户管理 ✅
- 简单的登录/注册
- 每人有自己的账号
- 密码重置（邮箱验证）

### 2. 对话功能 ✅
- 发送命令给 Claude Code
- 显示返回结果
- 保存历史对话
- 搜索历史记录

### 3. 文件处理 ✅
- 上传日志文件
- 文件列表管理
- 自动清理旧文件（30天）

### 4. 基础统计 ✅
- 每月使用次数
- Token 消耗量
- 简单的图表展示

### 不做的功能 ❌
- 实时协作
- 复杂的权限系统
- API 开放
- 移动端 APP

## 数据库设计（精简版）

```sql
-- 用户表（简单够用）
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE,
    password VARCHAR(255),
    email VARCHAR(100),
    created_at DATETIME
);

-- 对话记录表
CREATE TABLE conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    title VARCHAR(255),
    content TEXT,        -- 存储整个对话的JSON
    tokens_used INT,
    created_at DATETIME,
    INDEX idx_user_id (user_id)
);

-- 文件表
CREATE TABLE files (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    filename VARCHAR(255),
    file_path VARCHAR(500),
    created_at DATETIME,
    INDEX idx_user_created (user_id, created_at)
);
```

## API 设计（最简化）

```
# 认证
POST /api/login          - 登录
POST /api/register       - 注册
POST /api/logout         - 登出

# 核心功能
POST /api/chat           - 发送消息给 Claude Code
GET  /api/conversations  - 获取历史对话
POST /api/upload         - 上传文件
GET  /api/stats          - 获取统计数据
```





