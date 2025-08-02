# Claude Platform Frontend

基于 Vue 3 + Element Plus 的 Claude Platform 前端管理界面。

## 技术栈

- **Vue 3** - 渐进式 JavaScript 框架
- **Element Plus** - 基于 Vue 3 的桌面端组件库
- **Vue Router** - Vue.js 官方路由管理器
- **Pinia** - Vue 的官方状态管理库
- **Axios** - 基于 Promise 的 HTTP 库
- **Vite** - 新一代前端构建工具
- **ECharts** - 数据可视化图表库

## 功能特性

### 🔐 用户认证
- JWT Token 登录
- 自动登录状态检查
- 权限路由守卫

### 💬 对话功能
- 实时对话界面
- 文件上传支持
- 对话历史管理
- Token 使用量显示

### 📊 数据统计
- 使用趋势图表
- Token 消耗统计
- 个人使用记录

### 👥 用户管理（管理员）
- 用户列表管理
- 创建/编辑用户
- Token 额度调整
- 账号状态控制

### ⚙️ 系统设置（管理员）
- 基础系统配置
- 文件上传设置
- 安全策略配置
- 数据清理管理

### 📥 客户端下载
- 自动系统检测
- 多平台客户端下载
- 配置指导

## 快速开始

### 环境要求

- Node.js >= 16.0.0
- npm >= 8.0.0

### 安装依赖

\`\`\`bash
cd claude-platform-frontend
npm install
\`\`\`

### 开发环境启动

\`\`\`bash
npm run dev
\`\`\`

项目将在 http://localhost:8081 启动

### 生产环境构建

\`\`\`bash
npm run build
\`\`\`

构建产物将生成在 `dist` 目录

### 预览构建结果

\`\`\`bash
npm run preview
\`\`\`

## 项目结构

\`\`\`
claude-platform-frontend/
├── public/                 # 静态资源
├── src/
│   ├── components/         # 公共组件
│   ├── layouts/           # 布局组件
│   │   └── Layout.vue     # 主布局
│   ├── views/             # 页面组件
│   │   ├── Login.vue      # 登录页
│   │   ├── Chat.vue       # 对话页
│   │   ├── Download.vue   # 下载页
│   │   ├── Statistics.vue # 统计页
│   │   ├── AccountManage.vue    # 账号管理
│   │   ├── CreateAccount.vue    # 创建账号
│   │   └── SystemSettings.vue   # 系统设置
│   ├── router/            # 路由配置
│   │   └── index.js
│   ├── stores/            # 状态管理
│   │   └── user.js        # 用户状态
│   ├── utils/             # 工具函数
│   │   └── api.js         # API 封装
│   ├── styles/            # 样式文件
│   │   └── index.scss     # 全局样式
│   ├── App.vue            # 根组件
│   └── main.js            # 入口文件
├── index.html             # HTML 模板
├── vite.config.js         # Vite 配置
└── package.json           # 项目配置
\`\`\`

## 页面功能

### 🔑 登录页面 (/login)
- 用户名密码登录
- 记住登录状态
- 开发环境默认账号提示

### 💬 对话页面 (/dashboard)
- 新建对话会话
- 实时消息交互
- 文件上传功能
- 对话历史记录
- Token 使用量显示

### 📥 下载客户端 (/download)
- 自动检测操作系统
- 客户端下载链接
- 配置指导说明
- 连接状态检查

### 📊 使用统计 (/statistics)
- Token 使用统计卡片
- 使用趋势图表
- 详细使用记录表格
- 数据筛选和分页

### 👤 账号管理 (/account-manage) 【管理员】
- 用户列表查看
- 搜索和筛选
- 编辑用户信息
- 启用/禁用账号
- 重置用户密码
- 删除用户账号

### ➕ 开通账号 (/create-account) 【管理员】
- 创建新用户表单
- 角色权限设置
- Token 额度分配
- 账号预览
- 快速配置模板

### ⚙️ 系统设置 (/system-settings) 【管理员】
- 基础系统配置
- 文件上传设置
- 安全策略管理  
- 数据清理配置
- 系统状态监控

## API 接口

前端通过 Vite 代理将 `/api` 请求转发到后端服务（默认 http://localhost:10088）

### 主要接口：
- `POST /api/auth/login` - 用户登录
- `GET /api/user/info` - 获取用户信息
- `POST /api/user/change-password` - 修改密码
- `GET /api/statistics/my` - 个人统计
- `GET /api/system/default-settings` - 系统配置

## 开发说明

### 状态管理
使用 Pinia 管理全局状态，主要包括：
- 用户登录状态
- 用户信息
- JWT Token

### 路由权限
- 公开路由：登录页
- 认证路由：需要登录的页面
- 管理员路由：需要管理员权限的页面

### 响应式处理
Axios 拦截器统一处理：
- 自动添加 Authorization 头
- 统一错误处理
- 登录过期自动跳转

### 组件设计
- 采用 Vue 3 Composition API
- Element Plus 组件库
- 响应式布局设计
- 统一的样式规范

## 默认账号

开发环境默认管理员账号：
- 用户名：`admin`
- 密码：`123456`

## 浏览器支持

- Chrome >= 87
- Firefox >= 78
- Safari >= 14
- Edge >= 88

## 注意事项

1. **开发环境**：确保后端服务运行在 http://localhost:10088
2. **生产环境**：需要配置正确的 API 地址
3. **权限控制**：管理员功能只对 ROLE_ADMIN 角色开放
4. **Token 管理**：登录状态存储在 localStorage 中
5. **文件上传**：支持多种文件格式，最大 50MB

## 更新日志

### v1.0.0 (2025-01-01)
- 初始版本发布
- 完整的用户认证系统
- 对话功能界面
- 管理员功能模块
- 系统设置界面
- 响应式设计适配

## 许可证

MIT License