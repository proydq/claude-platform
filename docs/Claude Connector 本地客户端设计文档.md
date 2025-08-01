# Claude Connector 本地客户端设计文档

## 项目概述

### 定位
一个轻量级的本地客户端，负责连接云端服务和本地 Claude Code，作为两者之间的桥梁。

### 核心功能
1. 连接云端 WebSocket 服务
2. 接收云端指令
3. 执行本地 Claude Code 命令
4. 返回执行结果

## 项目目录结构

```
claude-connector/
├── cmd/
│   └── main.go                 # 程序入口
├── internal/
│   ├── config/
│   │   └── config.go          # 配置加载和管理
│   ├── executor/
│   │   └── executor.go        # Claude Code 执行器
│   ├── ws/
│   │   └── client.go          # WebSocket 客户端
│   └── models/
│       └── message.go         # 消息结构体定义
├── configs/
│   └── config.yaml            # 默认配置文件
├── scripts/
│   ├── build.sh              # Linux/Mac 构建脚本
│   └── build.bat             # Windows 构建脚本
├── go.mod                    # Go 模块定义
├── go.sum                    # 依赖版本锁定
├── .gitignore
└── README.md
```

## 依赖库

```go
// go.mod 文件内容
module claude-connector

go 1.20

require (
    github.com/gorilla/websocket v1.5.0   // WebSocket 通信
    github.com/spf13/viper v1.16.0       // 配置文件管理
    github.com/sirupsen/logrus v1.9.3    // 日志库
)
```

## 模块详细设计

### 1. 主程序入口 (cmd/main.go)

**功能职责**：
- 初始化日志
- 加载配置文件
- 创建 WebSocket 连接
- 启动消息处理循环
- 处理程序退出信号

**主要流程**：
```
1. 初始化日志系统
2. 加载配置文件（config.yaml）
3. 创建 Claude 执行器实例
4. 连接到云端 WebSocket 服务
5. 进入消息循环
6. 优雅退出
```

### 2. 配置管理 (internal/config/config.go)

**配置结构**：
```go
type Config struct {
    Server struct {
        URL   string // WebSocket 服务地址
        Token string // 认证令牌
    }
    Claude struct {
        Command string // Claude 命令路径，默认 "claude"
        Timeout int    // 执行超时时间（秒）
    }
    Client struct {
        ID   string // 客户端标识
        Name string // 客户端名称
    }
}
```

**配置文件示例** (configs/config.yaml)：
```yaml
server:
  url: "ws://localhost:8080/ws"
  token: "your-auth-token"

claude:
  command: "claude"
  timeout: 300  # 5分钟超时

client:
  id: "client-001"
  name: "开发环境客户端"
```

### 3. Claude 执行器 (internal/executor/executor.go)

**主要功能**：
- 执行 Claude Code 命令
- 管理工作目录
- 处理文件上传
- 捕获输出和错误
- 超时控制

**核心方法**：
```go
// 执行命令
Execute(command string, workDir string) (output string, err error)

// 保存上传的文件
SaveFile(filename string, content []byte) (filepath string, err error)

// 清理临时文件
CleanupFiles(age time.Duration) error
```

### 4. WebSocket 客户端 (internal/ws/client.go)

**主要功能**：
- 建立 WebSocket 连接
- 自动重连机制
- 心跳保活
- 消息收发

**核心方法**：
```go
// 连接服务器
Connect() error

// 发送消息
Send(message Message) error

// 接收消息
Receive() (Message, error)

// 关闭连接
Close() error
```

**重连策略**：
- 初次重连等待 5 秒
- 每次失败等待时间翻倍
- 最大等待时间 5 分钟

### 5. 消息模型 (internal/models/message.go)

**消息结构**：
```go
// 请求消息
type Request struct {
    ID        string            `json:"id"`
    Type      string            `json:"type"`      // "execute", "upload", "ping"
    Command   string            `json:"command"`
    Files     map[string]string `json:"files"`     // 文件名:内容(base64)
    Timestamp int64             `json:"timestamp"`
}

// 响应消息
type Response struct {
    ID        string `json:"id"`
    Success   bool   `json:"success"`
    Output    string `json:"output"`
    Error     string `json:"error,omitempty"`
    Timestamp int64  `json:"timestamp"`
}
```

## 核心流程

### 启动流程
```
1. 读取配置文件
2. 验证 Claude Code 是否可用
3. 连接 WebSocket 服务器
4. 发送客户端注册信息
5. 开始接收和处理消息
```

### 消息处理流程
```
1. 接收 WebSocket 消息
2. 解析消息类型
3. 如果有文件，先保存到临时目录
4. 执行 Claude Code 命令
5. 收集输出结果
6. 发送响应给服务器
7. 清理临时文件
```

### 错误处理
- 网络断开：自动重连
- 命令超时：强制终止并返回超时错误
- 文件过大：拒绝处理，返回错误
- Claude 不可用：启动时检查，运行时报错

## 构建和发布

### 构建命令
```bash
# Windows 64位
GOOS=windows GOARCH=amd64 go build -o dist/claude-connector-windows.exe ./cmd/main.go

# macOS Intel
GOOS=darwin GOARCH=amd64 go build -o dist/claude-connector-mac-intel ./cmd/main.go

# macOS M1/M2
GOOS=darwin GOARCH=arm64 go build -o dist/claude-connector-mac-arm ./cmd/main.go

# Linux
GOOS=linux GOARCH=amd64 go build -o dist/claude-connector-linux ./cmd/main.go
```

### 运行方式
```bash
# 使用默认配置
./claude-connector

# 指定配置文件
./claude-connector -config /path/to/config.yaml

# 查看版本
./claude-connector -version
```

## 日志设计

### 日志级别
- **INFO**：正常操作日志
- **WARN**：警告信息（如重连）
- **ERROR**：错误信息（但程序可继续运行）
- **FATAL**：致命错误（程序退出）

### 日志文件
- 位置：程序所在目录的 `logs` 文件夹
- 格式：`claude-connector-2024-01-15.log`
- 保留：最近 7 天的日志

## 安全考虑

1. **认证**：使用 Token 进行身份验证
2. **加密**：生产环境使用 WSS（WebSocket Secure）
3. **文件隔离**：每个会话使用独立的临时目录
4. **资源限制**：限制文件大小和命令执行时间

## 配置示例

### 开发环境
```yaml
server:
  url: "ws://localhost:8080/ws"
  token: "dev-token-123"

claude:
  command: "claude"
  timeout: 300

client:
  id: "dev-client"
  name: "开发测试"
```

### 生产环境
```yaml
server:
  url: "wss://api.example.com/ws"
  token: "${AUTH_TOKEN}"  # 从环境变量读取

claude:
  command: "claude"
  timeout: 600

client:
  id: "${HOSTNAME}"      # 使用主机名作为 ID
  name: "生产客户端"
```

## 版本规划

### v1.0 - 基础版本
- [x] WebSocket 连接
- [x] 执行 Claude Code
- [x] 基本错误处理
- [x] 配置文件支持

### v1.1 - 增强版本（如需要）
- [ ] 断点续传大文件
- [ ] 命令执行进度反馈
- [ ] 本地命令缓存
- [ ] 图形界面托盘程序