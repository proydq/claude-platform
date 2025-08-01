# Claude Code Platform 前后端详细设计文档

## 项目概述

基于 Vue3 + Spring Boot 的 Web 平台，用于管理和使用 Claude Code。系统采用管理员开通账号的方式，支持 Token 限额管理。

## 技术栈

### 前端
- Vue 3.x
- Element Plus
- Axios
- Vue Router
- Pinia（状态管理）

### 后端
- Spring Boot 2.7.6
- Java 8
- Spring Data JPA
- Spring Security
- JWT Token
- MySQL 8.0
- Lombok
- WebSocket

## 前端页面设计

### 1. 登录页面 (Login.vue)
**功能说明**：
- 用户名密码登录
- JWT Token 存储
- 登录失败提示

**页面布局**：
```
┌─────────────────────────────────────┐
│           Claude Platform           │
│                                     │
│         ┌─────────────┐             │
│         │  用户名      │             │
│         └─────────────┘             │
│         ┌─────────────┐             │
│         │  密码        │             │
│         └─────────────┘             │
│                                     │
│         [    登 录    ]             │
│                                     │
│      联系管理员开通账号               │
└─────────────────────────────────────┘
```

### 2. 主框架页面 (Layout.vue)
**功能说明**：
- 顶部导航栏
- 左侧菜单（根据角色动态显示）
- 内容展示区

**页面布局**：
```
┌─────────────────────────────────────────────────┐
│ Logo  Claude Platform    张三  剩余:5000  [退出] │
├────────────┬────────────────────────────────────┤
│            │                                    │
│ 使用对话    │                                    │
│ 下载客户端  │         <router-view/>             │
│ 使用统计    │                                    │
│ ---------- │                                    │
│ 账号管理    │      (根据不同菜单显示不同内容)      │
│ 开通账号    │                                    │
│ 系统设置    │                                    │
│            │                                    │
└────────────┴────────────────────────────────────┘
```

### 3. 对话页面 (Chat.vue)
**功能说明**：
- 发送命令到 Claude Code
- 显示执行结果
- 文件上传
- 历史对话管理
- Token 消耗提示

**页面布局**：
```
┌──────────────┬──────────────────────────────────┐
│  历史对话     │        当前对话                   │
│              │                                  │
│ [新建对话]    │  本次消耗: 150 Tokens            │
│              │  ┌────────────────────────────┐ │
│ 2024-01-15   │  │ User: 分析 error.log       │ │
│ 分析日志文件  │  │                            │ │
│              │  │ Claude: 正在分析...         │ │
│ 2024-01-14   │  │ [代码块展示区域]            │ │
│ 代码优化      │  │                            │ │
│              │  └────────────────────────────┘ │
│ [加载更多]    │                                  │
│              │  [上传文件] [输入框] [发送]       │
└──────────────┴──────────────────────────────────┘
```

### 4. 下载客户端页面 (Download.vue)
**功能说明**：
- 检测操作系统
- 下载对应客户端
- 显示配置说明
- 连接状态检查

**页面布局**：
```
┌─────────────────────────────────────────────────┐
│              下载客户端                          │
├─────────────────────────────────────────────────┤
│                                                 │
│  当前连接状态: ● 未连接                          │
│                                                 │
│  检测到您的系统: Windows 64位                    │
│                                                 │
│  ┌──────────────────────────────┐              │
│  │   [下载 Windows 客户端]       │              │
│  └──────────────────────────────┘              │
│                                                 │
│  配置说明:                                      │
│  1. 下载并解压文件                              │
│  2. 修改 config.yaml 中的服务器地址              │
│  3. 运行 claude-connector.exe                   │
│                                                 │
│  其他版本下载:                                  │
│  • macOS Intel  [下载]                         │
│  • macOS M1/M2  [下载]                         │
│  • Linux x64    [下载]                         │
└─────────────────────────────────────────────────┘
```

### 5. 使用统计页面 (Statistics.vue)
**功能说明**：
- 个人使用统计
- Token 消耗趋势
- 剩余额度显示

**页面布局**：
```
┌─────────────────────────────────────────────────┐
│              使用统计                            │
├─────────────────────────────────────────────────┤
│                                                 │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐          │
│  │本月使用  │ │本月消耗  │ │剩余额度  │          │
│  │ 125次   │ │ 3,500   │ │ 1,500   │          │
│  └─────────┘ └─────────┘ └─────────┘          │
│                                                 │
│  使用趋势图:                                    │
│  ┌────────────────────────────────┐            │
│  │         折线图展示区域           │            │
│  └────────────────────────────────┘            │
│                                                 │
│  最近使用记录:                                  │
│  ┌────────────────────────────────┐            │
│  │ 时间  │ 类型 │ 消耗 │ 操作    │            │
│  │ 10:30 │ 对话 │ 150  │ 查看    │            │
│  └────────────────────────────────┘            │
└─────────────────────────────────────────────────┘
```

### 6. 账号管理页面 (AccountManage.vue) 【管理员】
**功能说明**：
- 查看所有账号
- 修改 Token 额度
- 启用/禁用账号
- 重置密码

**页面布局**：
```
┌─────────────────────────────────────────────────┐
│              账号管理                            │
├─────────────────────────────────────────────────┤
│                                                 │
│  搜索: [_____________] [搜索]                   │
│                                                 │
│  ┌────────────────────────────────────────┐    │
│  │用户名│姓名│额度│已用│状态│操作         │    │
│  ├────────────────────────────────────────┤    │
│  │zhang3│张三│5000│3500│启用│[编辑][禁用] │    │
│  │ li4  │李四│3000│1200│启用│[编辑][禁用] │    │
│  │wang5 │王五│5000│5000│禁用│[编辑][启用] │    │
│  └────────────────────────────────────────┘    │
│                                                 │
│  [上一页] 1 2 3 [下一页]                        │
└─────────────────────────────────────────────────┘
```

### 7. 开通账号页面 (CreateAccount.vue) 【管理员】
**功能说明**：
- 创建新用户
- 设置初始密码
- 分配 Token 额度
- 设置用户角色

**页面布局**：
```
┌─────────────────────────────────────────────────┐
│              开通账号                            │
├─────────────────────────────────────────────────┤
│                                                 │
│  基本信息:                                      │
│  用户名: [_____________] *                      │
│  姓名:   [_____________] *                      │
│  邮箱:   [_____________]                        │
│                                                 │
│  账号设置:                                      │
│  初始密码: [_____________] *                    │
│  确认密码: [_____________] *                    │
│                                                 │
│  权限设置:                                      │
│  用户角色: ○ 普通用户  ○ 管理员                 │
│  Token额度: [_____________] 个/月               │
│                                                 │
│         [取消]  [确认开通]                       │
└─────────────────────────────────────────────────┘
```

### 8. 编辑账号页面 (EditAccount.vue) 【管理员】
**功能说明**：
- 修改用户信息
- 调整 Token 额度
- 重置密码

**页面布局**：
```
┌─────────────────────────────────────────────────┐
│              编辑账号                            │
├─────────────────────────────────────────────────┤
│                                                 │
│  用户信息:                                      │
│  用户名: zhang3 (不可修改)                      │
│  姓名:   [张三_______]                         │
│  邮箱:   [zhang3@example.com]                  │
│                                                 │
│  额度管理:                                      │
│  月度额度: [5000_______] 个                     │
│  本月已用: 3500 个                              │
│  □ 追加临时额度: [_______] 个                   │
│                                                 │
│  密码重置:                                      │
│  □ 重置密码为: [_____________]                 │
│                                                 │
│         [取消]  [保存修改]                       │
└─────────────────────────────────────────────────┘
```

### 9. 系统设置页面 (SystemSettings.vue) 【管理员】
**功能说明**：
- 全局 Token 限制设置
- 文件上传限制
- 清理策略设置

**页面布局**：
```
┌─────────────────────────────────────────────────┐
│              系统设置                            │
├─────────────────────────────────────────────────┤
│                                                 │
│  Token 设置:                                    │
│  默认月度额度: [_____] 个                        │
│  单次最大消耗: [_____] 个                        │
│                                                 │
│  文件设置:                                      │
│  最大上传大小: [_____] MB                       │
│  文件保留时间: [_____] 小时                     │
│                                                 │
│  清理设置:                                      │
│  对话历史保留: [_____] 天                       │
│  □ 自动清理过期文件                             │
│                                                 │
│              [保存设置]                          │
└─────────────────────────────────────────────────┘
```

### 10. 个人中心页面 (Profile.vue) 【所有用户】
**功能说明**：
- 查看个人信息
- 修改密码
- 查看额度使用情况

**页面布局**：
```
┌─────────────────────────────────────────────────┐
│              个人中心                            │
├─────────────────────────────────────────────────┤
│                                                 │
│  基本信息:                                      │
│  用户名: zhang3                                 │
│  姓名: 张三                                     │
│  邮箱: zhang3@example.com                      │
│                                                 │
│  额度信息:                                      │
│  月度额度: 5000 个                              │
│  已使用: 3500 个                                │
│  剩余: 1500 个                                  │
│                                                 │
│  修改密码:                                      │
│  当前密码: [_____________]                      │
│  新密码:   [_____________]                      │
│  确认密码: [_____________]                      │
│                                                 │
│              [保存修改]                          │
└─────────────────────────────────────────────────┘
```

## 前端目录结构

```
frontend/
├── public/
├── src/
│   ├── api/                    # API 接口定义
│   │   ├── auth.js            # 登录相关
│   │   ├── chat.js            # 对话相关
│   │   ├── user.js            # 用户管理
│   │   ├── statistics.js      # 统计相关
│   │   └── system.js          # 系统设置
│   ├── router/
│   │   └── index.js           # 路由配置，包含权限控制
│   ├── store/
│   │   ├── auth.js            # 认证状态管理
│   │   ├── user.js            # 用户信息
│   │   └── websocket.js       # WebSocket 状态
│   ├── utils/
│   │   ├── request.js         # axios 封装，JWT token 处理
│   │   ├── auth.js            # 权限判断工具
│   │   └── websocket.js       # WebSocket 连接管理
│   ├── views/
│   │   ├── Login.vue          # 登录页
│   │   ├── Layout.vue         # 主框架
│   │   ├── Chat.vue           # 对话页面
│   │   ├── Download.vue       # 下载页面
│   │   ├── Statistics.vue     # 统计页面
│   │   ├── Profile.vue        # 个人中心
│   │   ├── admin/             # 管理员页面
│   │   │   ├── AccountManage.vue   # 账号管理
│   │   │   ├── CreateAccount.vue   # 开通账号
│   │   │   ├── EditAccount.vue     # 编辑账号
│   │   │   └── SystemSettings.vue  # 系统设置
│   │   └── 404.vue            # 404页面
│   ├── components/            # 公共组件
│   │   ├── FileUpload.vue     # 文件上传
│   │   ├── CodeDisplay.vue    # 代码展示
│   │   └── TokenProgress.vue  # Token 使用进度
│   ├── App.vue
│   └── main.js
└── package.json
```

## 后端目录结构

```
backend/
└── src/main/java/com/example/claudeplatform/
    ├── config/
    │   ├── SecurityConfig.java        # Spring Security 配置
    │   ├── JwtAuthenticationFilter.java # JWT 认证过滤器
    │   ├── WebSocketConfig.java       # WebSocket 配置
    │   ├── JpaConfig.java            # JPA 配置
    │   └── CorsConfig.java           # 跨域配置
    ├── controller/
    │   ├── AuthController.java        # 认证控制器(登录)
    │   ├── UserController.java        # 用户管理(增删改查)
    │   ├── ChatController.java        # 对话管理
    │   ├── FileController.java        # 文件上传下载
    │   ├── StatisticsController.java  # 统计数据
    │   ├── SystemController.java      # 系统设置
    │   └── DownloadController.java    # 客户端下载
    ├── entity/
    │   ├── User.java                  # 用户实体(role字段直接存储)
    │   ├── Conversation.java          # 对话实体
    │   ├── FileRecord.java            # 文件记录
    │   ├── TokenUsage.java            # Token使用记录
    │   └── SystemConfig.java          # 系统配置
    ├── repository/
    │   ├── UserRepository.java        # 用户数据访问
    │   ├── ConversationRepository.java # 对话数据访问
    │   ├── FileRepository.java        # 文件数据访问
    │   ├── TokenUsageRepository.java  # Token记录访问
    │   └── SystemConfigRepository.java # 系统配置访问
    ├── service/
    │   ├── AuthService.java           # 认证服务
    │   ├── UserService.java           # 用户管理服务
    │   ├── ChatService.java           # 对话服务
    │   ├── FileService.java           # 文件服务
    │   ├── TokenService.java          # Token管理服务
    │   ├── StatisticsService.java     # 统计服务
    │   └── SystemService.java         # 系统设置服务
    ├── dto/
    │   ├── request/                   # 请求DTO
    │   │   ├── LoginRequest.java      # 登录请求
    │   │   ├── CreateUserRequest.java # 创建用户请求
    │   │   ├── UpdateUserRequest.java # 更新用户请求
    │   │   └── ChatRequest.java       # 对话请求
    │   └── response/                  # 响应DTO
    │       ├── ApiResponse.java       # 统一响应格式
    │       ├── LoginResponse.java     # 登录响应(含token)
    │       ├── UserInfo.java          # 用户信息
    │       └── StatisticsData.java    # 统计数据
    ├── security/
    │   ├── JwtTokenProvider.java      # JWT工具类
    │   ├── CustomUserDetails.java     # 自定义用户详情
    │   ├── CustomUserDetailsService.java # 用户详情服务
    │   └── SecurityUtils.java         # 安全工具类
    ├── websocket/
    │   ├── ChatWebSocketHandler.java  # 对话WebSocket处理
    │   ├── ConnectionManager.java     # 连接管理器
    │   └── MessageHandler.java        # 消息处理器
    ├── exception/
    │   ├── BusinessException.java     # 业务异常
    │   ├── UnauthorizedException.java # 未授权异常
    │   └── GlobalExceptionHandler.java # 全局异常处理
    ├── utils/
    │   ├── FileUtil.java              # 文件工具类
    │   └── DateUtil.java              # 日期工具类
    └── ClaudePlatformApplication.java # 主启动类
```

## 权限设计

### 角色定义
- **ROLE_ADMIN**: 管理员，可以管理用户、查看所有数据
- **ROLE_USER**: 普通用户，只能使用对话功能和查看自己的数据

### 接口权限
```
需要 ROLE_ADMIN:
- POST   /api/users              创建用户
- GET    /api/users              获取用户列表
- PUT    /api/users/{id}         更新用户
- DELETE /api/users/{id}         删除用户
- GET    /api/system/settings    获取系统设置
- PUT    /api/system/settings    更新系统设置

需要登录即可:
- GET    /api/profile            获取个人信息
- PUT    /api/profile/password   修改密码
- POST   /api/chat               发送对话
- GET    /api/statistics/my      获取个人统计
```

## 数据库表结构

### users 表（用户表）
```sql
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,                    -- UUID主键
    username VARCHAR(50) UNIQUE NOT NULL,          -- 用户名
    user_password VARCHAR(255) NOT NULL,           -- 密码（避免使用password关键字）
    real_name VARCHAR(50),                         -- 真实姓名
    email VARCHAR(100),                            -- 邮箱
    user_role VARCHAR(20) NOT NULL,                -- 角色：ROLE_ADMIN/ROLE_USER
    token_limit INT DEFAULT 5000,                  -- 月度Token额度
    user_status VARCHAR(20) DEFAULT 'ACTIVE',      -- 状态：ACTIVE/DISABLED
    created_time BIGINT NOT NULL,                  -- 创建时间戳
    updated_time BIGINT,                           -- 更新时间戳
    last_login_time BIGINT                         -- 最后登录时间戳
);
```

### conversations 表（对话表）
```sql
CREATE TABLE conversations (
    id VARCHAR(36) PRIMARY KEY,                    -- UUID主键
    user_id VARCHAR(36) NOT NULL,                  -- 用户ID
    title VARCHAR(200),                            -- 对话标题
    content TEXT,                                  -- 对话内容（JSON格式）
    tokens_used INT DEFAULT 0,                     -- 消耗的Token数
    created_time BIGINT NOT NULL,                  -- 创建时间戳
    updated_time BIGINT,                           -- 更新时间戳
    INDEX idx_user_time (user_id, created_time)    -- 用户和时间的联合索引
);
```

### files 表（文件记录表）
```sql
CREATE TABLE files (
    id VARCHAR(36) PRIMARY KEY,                    -- UUID主键
    user_id VARCHAR(36) NOT NULL,                  -- 用户ID
    file_name VARCHAR(255) NOT NULL,               -- 文件名
    file_path VARCHAR(500) NOT NULL,               -- 文件路径
    file_size BIGINT,                              -- 文件大小（字节）
    file_type VARCHAR(50),                         -- 文件类型
    created_time BIGINT NOT NULL,                  -- 创建时间戳
    expire_time BIGINT,                            -- 过期时间戳
    INDEX idx_user_time (user_id, created_time)    -- 用户和时间的联合索引
);
```

### token_usage 表（Token使用记录表）
```sql
CREATE TABLE token_usage (
    id VARCHAR(36) PRIMARY KEY,                    -- UUID主键
    user_id VARCHAR(36) NOT NULL,                  -- 用户ID
    conversation_id VARCHAR(36),                   -- 关联的对话ID
    tokens_count INT NOT NULL,                     -- 使用的Token数量
    usage_type VARCHAR(20),                        -- 使用类型：CHAT/FILE_ANALYSIS
    usage_time BIGINT NOT NULL,                    -- 使用时间戳
    INDEX idx_user_time (user_id, usage_time)      -- 用户和时间的联合索引
);
```

### system_config 表（系统配置表）
```sql
CREATE TABLE system_config (
    id VARCHAR(36) PRIMARY KEY,                    -- UUID主键
    config_key VARCHAR(100) UNIQUE NOT NULL,       -- 配置键
    config_value VARCHAR(500),                     -- 配置值
    config_desc VARCHAR(200),                      -- 配置描述
    created_time BIGINT NOT NULL,                  -- 创建时间戳
    updated_time BIGINT                            -- 更新时间戳
);
```

### 初始化数据
```sql
-- 插入默认系统配置
INSERT INTO system_config (id, config_key, config_value, config_desc, created_time) VALUES
(UUID(), 'default_token_limit', '5000', '默认月度Token额度', UNIX_TIMESTAMP() * 1000),
(UUID(), 'max_file_size', '10485760', '最大文件大小（字节）', UNIX_TIMESTAMP() * 1000),
(UUID(), 'file_expire_hours', '24', '文件过期时间（小时）', UNIX_TIMESTAMP() * 1000),
(UUID(), 'max_tokens_per_request', '1000', '单次请求最大Token数', UNIX_TIMESTAMP() * 1000);

-- 插入默认管理员账号（密码需要加密）
INSERT INTO users (id, username, user_password, real_name, user_role, token_limit, created_time) VALUES
(UUID(), 'admin', '$2a$10$...', '系统管理员', 'ROLE_ADMIN', 999999, UNIX_TIMESTAMP() * 1000);
```

### 注意事项
1. 所有主键使用 VARCHAR(36) 存储 UUID
2. 所有时间字段使用 BIGINT 存储毫秒级时间戳
3. 避免使用 MySQL 关键字（如 password 改为 user_password）
4. 所有表之间的关联通过 ID 手动管理，不使用外键约束
5. 适当添加索引以提高查询性能

注：所有表之间的关联通过ID手动管理，不使用JPA的关联注解

## WebSocket 消息格式

### 客户端连接认证
```json
{
  "type": "auth",
  "token": "JWT token",
  "clientType": "user" // 或 "client"
}
```

### 对话消息
```json
{
  "type": "message",
  "conversationId": "123",
  "content": "用户输入的内容",
  "files": ["fileId1", "fileId2"]
}
```

### 响应消息
```json
{
  "type": "response",
  "conversationId": "123",
  "content": "Claude的响应",
  "tokensUsed": 150,
  "success": true
}
```

## 前后端接口对应关系

### 1. 登录页面 (Login.vue)
**调用接口**：
- `POST /api/auth/login` - 用户登录，返回JWT token

**数据流程**：
```
用户输入 → 点击登录 → 调用登录接口 → 获取token → 存储到localStorage → 跳转主页
```

### 2. 主框架页面 (Layout.vue)
**调用接口**：
- `GET /api/profile` - 获取当前用户信息（显示用户名、剩余额度）
- `POST /api/auth/logout` - 退出登录

**数据流程**：
```
页面加载 → 调用profile接口 → 显示用户信息和剩余额度
点击退出 → 调用logout接口 → 清除token → 跳转登录页
```

### 3. 对话页面 (Chat.vue)
**调用接口**：
- `GET /api/chat/conversations?page=1&size=20` - 获取历史对话列表
- `POST /api/chat/conversations` - 创建新对话
- `GET /api/chat/conversations/{id}` - 获取对话详情
- `POST /api/files/upload` - 上传文件
- `WebSocket /ws/chat` - 实时对话通信

**数据流程**：
```
页面加载 → 获取历史对话列表 → 建立WebSocket连接
用户输入 → 通过WebSocket发送 → 显示Claude响应 → 自动保存对话
上传文件 → 调用upload接口 → 获取fileId → 发送对话时带上fileId
```

### 4. 下载客户端页面 (Download.vue)
**调用接口**：
- `GET /api/download/detect` - 检测用户操作系统
- `GET /api/download/client/{platform}` - 下载对应平台客户端
- `GET /api/download/check-connection` - 检查客户端连接状态

**数据流程**：
```
页面加载 → 检测系统 → 显示推荐下载
定时轮询 → 检查连接状态 → 更新连接指示器
点击下载 → 调用download接口 → 浏览器下载文件
```

### 5. 使用统计页面 (Statistics.vue)
**调用接口**：
- `GET /api/statistics/my/summary` - 获取个人统计摘要
- `GET /api/statistics/my/detail?startDate=&endDate=` - 获取详细使用记录
- `GET /api/statistics/my/trend?days=30` - 获取使用趋势数据

**数据流程**：
```
页面加载 → 并行调用三个接口 → 渲染统计卡片、趋势图、详细列表
```

### 6. 账号管理页面 (AccountManage.vue) 【管理员】
**调用接口**：
- `GET /api/users?page=1&size=20&keyword=` - 获取用户列表
- `PUT /api/users/{id}/status` - 启用/禁用账号
- `PUT /api/users/{id}/token-limit` - 修改额度

**数据流程**：
```
页面加载 → 获取用户列表
搜索 → 调用users接口带keyword参数
点击禁用 → 调用status接口 → 刷新列表
点击编辑 → 跳转编辑页面
```

### 7. 开通账号页面 (CreateAccount.vue) 【管理员】
**调用接口**：
- `POST /api/users` - 创建新用户
- `GET /api/users/check-username?username=` - 检查用户名是否存在

**数据流程**：
```
输入用户名 → 失焦时调用check接口 → 提示是否可用
填写表单 → 提交 → 调用create接口 → 成功后跳转账号管理页
```

### 8. 编辑账号页面 (EditAccount.vue) 【管理员】
**调用接口**：
- `GET /api/users/{id}` - 获取用户详情
- `PUT /api/users/{id}` - 更新用户信息
- `PUT /api/users/{id}/password` - 重置密码
- `PUT /api/users/{id}/extra-token` - 追加临时额度

**数据流程**：
```
页面加载 → 获取用户详情 → 填充表单
修改信息 → 提交 → 调用update接口
重置密码 → 调用password接口
追加额度 → 调用extra-token接口
```

### 9. 系统设置页面 (SystemSettings.vue) 【管理员】
**调用接口**：
- `GET /api/system/settings` - 获取系统设置
- `PUT /api/system/settings` - 更新系统设置

**数据流程**：
```
页面加载 → 获取当前设置 → 填充表单
修改设置 → 保存 → 调用update接口 → 提示保存成功
```

### 10. 个人中心页面 (Profile.vue)
**调用接口**：
- `GET /api/profile` - 获取个人信息
- `PUT /api/profile/password` - 修改密码
- `GET /api/statistics/my/summary` - 获取额度使用情况

**数据流程**：
```
页面加载 → 获取个人信息和额度统计
修改密码 → 验证旧密码 → 调用password接口 → 成功后提示
```

## API 接口汇总

### 认证相关
- `POST /api/auth/login` - 登录
- `POST /api/auth/logout` - 登出

### 用户管理（需要管理员权限）
- `POST /api/users` - 创建用户
- `GET /api/users` - 用户列表
- `GET /api/users/{id}` - 用户详情
- `PUT /api/users/{id}` - 更新用户
- `PUT /api/users/{id}/status` - 启用/禁用
- `PUT /api/users/{id}/password` - 重置密码
- `PUT /api/users/{id}/token-limit` - 修改额度
- `PUT /api/users/{id}/extra-token` - 追加额度
- `GET /api/users/check-username` - 检查用户名

### 个人信息
- `GET /api/profile` - 获取个人信息
- `PUT /api/profile/password` - 修改密码

### 对话功能
- `GET /api/chat/conversations` - 对话列表
- `POST /api/chat/conversations` - 创建对话
- `GET /api/chat/conversations/{id}` - 对话详情
- `DELETE /api/chat/conversations/{id}` - 删除对话
- `WebSocket /ws/chat` - 实时对话

### 文件管理
- `POST /api/files/upload` - 上传文件
- `GET /api/files/{id}` - 下载文件
- `DELETE /api/files/{id}` - 删除文件

### 客户端下载
- `GET /api/download/detect` - 检测系统
- `GET /api/download/client/{platform}` - 下载客户端
- `GET /api/download/check-connection` - 检查连接

### 统计数据
- `GET /api/statistics/my/summary` - 个人统计摘要
- `GET /api/statistics/my/detail` - 详细记录
- `GET /api/statistics/my/trend` - 使用趋势

### 系统设置（需要管理员权限）
- `GET /api/system/settings` - 获取设置
- `PUT /api/system/settings` - 更新设置