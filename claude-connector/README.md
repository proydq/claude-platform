# Claude Connector

Claude Connector 是一个轻量级的本地客户端，用于连接云端服务和本地 Claude Code。

## 功能特性

- 🔗 WebSocket 实时通信
- 🔄 自动重连机制
- 📁 文件上传支持
- ⏱️ 命令超时控制
- 📝 完整日志记录
- 🔒 Token 认证

## 快速开始

### 1. 下载

从 [Releases](https://github.com/your-repo/releases) 页面下载适合您系统的版本。

### 2. 配置

1. 将 `config.yaml.example` 重命名为 `config.yaml`
2. 编辑配置文件，填入服务器地址和认证令牌：

```yaml
server:
  url: "ws://your-server:8080/ws/connector"
  token: "your-auth-token"
```

### 3. 运行

Windows:
```bash
claude-connector.exe
```

Linux/Mac:
```bash
./claude-connector
```

### 命令行参数

```bash
# 指定配置文件
claude-connector -config /path/to/config.yaml

# 查看版本
claude-connector -version

# 设置日志级别
claude-connector -log-level debug
```

## 配置说明

```yaml
# 服务器配置
server:
  url: "ws://localhost:8080/ws/connector"  # WebSocket 服务地址
  token: "your-auth-token"                  # 认证令牌

# Claude 配置
claude:
  command: "claude"   # Claude 命令路径
  timeout: 300        # 执行超时（秒）

# 客户端配置
client:
  id: "connector-001"              # 客户端 ID
  name: "My Claude Connector"      # 客户端名称

# 日志配置
log:
  level: "info"                    # 日志级别: debug, info, warn, error
  file: "logs/claude-connector.log" # 日志文件路径
```

## 开发

### 环境要求

- Go 1.20+
- Claude Code 已安装并可用

### 构建

```bash
# 安装依赖
go mod download

# 构建当前平台
go build -o claude-connector ./cmd/main.go

# 构建所有平台
./scripts/build.sh  # Linux/Mac
.\scripts\build.bat # Windows
```

### 项目结构

```
claude-connector/
├── cmd/            # 程序入口
├── internal/       # 内部包
│   ├── config/    # 配置管理
│   ├── executor/  # Claude 执行器
│   ├── ws/        # WebSocket 客户端
│   └── models/    # 数据模型
├── configs/       # 配置文件
├── scripts/       # 构建脚本
└── logs/         # 日志目录
```

## 故障排除

### 连接失败

1. 检查服务器地址是否正确
2. 确认认证令牌是否有效
3. 检查防火墙设置

### Claude 命令不可用

1. 确认 Claude Code 已安装
2. 检查 PATH 环境变量
3. 在配置文件中指定完整路径

### 日志查看

日志文件位于 `logs/claude-connector.log`，可以通过以下方式查看：

```bash
# 实时查看日志（Linux/Mac）
tail -f logs/claude-connector.log

# 查看错误日志
grep ERROR logs/claude-connector.log
```

## License

MIT License