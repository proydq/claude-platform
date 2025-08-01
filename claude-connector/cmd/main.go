package main

import (
	"flag"
	"fmt"
	"os"
	"os/signal"
	"path/filepath"
	"syscall"
	"time"

	"claude-connector/internal/config"
	"claude-connector/internal/executor"
	"claude-connector/internal/models"
	"claude-connector/internal/ws"

	"github.com/sirupsen/logrus"
)

var (
	version = "1.0.0"
	logger  *logrus.Logger
)

func main() {
	// 解析命令行参数
	var (
		configPath  = flag.String("config", "", "配置文件路径")
		showVersion = flag.Bool("version", false, "显示版本信息")
		logLevel    = flag.String("log-level", "", "日志级别 (debug, info, warn, error)")
	)
	flag.Parse()

	if *showVersion {
		fmt.Printf("Claude Connector v%s\n", version)
		os.Exit(0)
	}

	// 初始化日志
	logger = initLogger(*logLevel)
	logger.Infof("Claude Connector v%s 启动", version)

	// 加载配置
	cfg, err := config.Load(*configPath)
	if err != nil {
		logger.Fatalf("加载配置失败: %v", err)
	}

	// 更新日志级别
	if cfg.Log.Level != "" {
		level, err := logrus.ParseLevel(cfg.Log.Level)
		if err == nil {
			logger.SetLevel(level)
		}
	}

	// 创建 Claude 执行器
	exec, err := executor.New(cfg.Claude.Command, cfg.Claude.Timeout, logger)
	if err != nil {
		logger.Fatalf("创建执行器失败: %v", err)
	}

	// 创建 WebSocket 客户端
	client := ws.New(cfg, logger)

	// 连接到服务器
	if err := client.Connect(); err != nil {
		logger.Fatalf("连接服务器失败: %v", err)
	}
	defer client.Close()

	// 启动消息处理
	go messageHandler(client, exec)

	// 启动定期清理任务
	go cleanupTask(exec)

	// 等待退出信号
	sigChan := make(chan os.Signal, 1)
	signal.Notify(sigChan, syscall.SIGINT, syscall.SIGTERM)

	sig := <-sigChan
	logger.Infof("收到信号 %v，正在退出...", sig)

	// 优雅退出
	if err := client.Close(); err != nil {
		logger.Errorf("关闭连接失败: %v", err)
	}

	logger.Info("程序已退出")
}

// initLogger 初始化日志
func initLogger(level string) *logrus.Logger {
	log := logrus.New()

	// 设置日志格式
	log.SetFormatter(&logrus.TextFormatter{
		TimestampFormat: "2006-01-02 15:04:05",
		FullTimestamp:   true,
	})

	// 设置日志级别
	if level != "" {
		lvl, err := logrus.ParseLevel(level)
		if err == nil {
			log.SetLevel(lvl)
		}
	} else {
		log.SetLevel(logrus.InfoLevel)
	}

	// 创建日志目录
	logDir := filepath.Dir("logs/claude-connector.log")
	if err := os.MkdirAll(logDir, 0755); err != nil {
		log.Warnf("创建日志目录失败: %v", err)
	}

	// 同时输出到文件和控制台
	logFile, err := os.OpenFile("logs/claude-connector.log", os.O_CREATE|os.O_WRONLY|os.O_APPEND, 0666)
	if err == nil {
		log.SetOutput(logFile)
		// 同时输出到控制台
		log.AddHook(&ConsoleHook{})
	}

	return log
}

// ConsoleHook 控制台输出钩子
type ConsoleHook struct{}

func (h *ConsoleHook) Levels() []logrus.Level {
	return logrus.AllLevels
}

func (h *ConsoleHook) Fire(entry *logrus.Entry) error {
	msg, err := entry.String()
	if err != nil {
		return err
	}
	fmt.Print(msg)
	return nil
}

// messageHandler 处理接收到的消息
func messageHandler(client *ws.Client, exec *executor.Executor) {
	for request := range client.Receive() {
		logger.Infof("处理请求: type=%s, id=%s", request.Type, request.ID)

		go func(req *models.Request) {
			var output string
			var err error

			switch req.Type {
			case models.TypeExecute:
				// 执行命令
				output, err = exec.Execute(req.Command, req.WorkDir, req.Files)

			case models.TypeUpload:
				// 仅上传文件的情况
				if len(req.Files) > 0 {
					output = fmt.Sprintf("已接收 %d 个文件", len(req.Files))
				} else {
					err = fmt.Errorf("没有文件需要上传")
				}

			default:
				err = fmt.Errorf("未知的请求类型: %s", req.Type)
			}

			// 构建响应
			response := models.NewResponse(req.ID, err == nil)
			if err != nil {
				response.Error = err.Error()
				logger.Errorf("处理请求失败: %v", err)
			} else {
				response.Output = output
			}

			// 发送响应
			if err := client.Send(response); err != nil {
				logger.Errorf("发送响应失败: %v", err)
			}
		}(request)
	}
}

// cleanupTask 定期清理任务
func cleanupTask(exec *executor.Executor) {
	ticker := time.NewTicker(1 * time.Hour)
	defer ticker.Stop()

	for range ticker.C {
		logger.Debug("执行清理任务")
		if err := exec.CleanupOldFiles(24 * time.Hour); err != nil {
			logger.Errorf("清理临时文件失败: %v", err)
		}
	}
}
