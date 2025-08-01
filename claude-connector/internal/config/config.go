package config

import (
	"fmt"
	"github.com/spf13/viper"
	"path/filepath"
)

// Config 配置结构
type Config struct {
	Server ServerConfig `yaml:"server"`
	Claude ClaudeConfig `yaml:"claude"`
	Client ClientConfig `yaml:"client"`
	Log    LogConfig    `yaml:"log"`
}

// ServerConfig 服务器配置
type ServerConfig struct {
	URL   string `yaml:"url"`   // WebSocket 服务地址
	Token string `yaml:"token"` // 认证令牌
}

// ClaudeConfig Claude 配置
type ClaudeConfig struct {
	Command string `yaml:"command"` // Claude 命令路径
	Timeout int    `yaml:"timeout"` // 执行超时时间（秒）
}

// ClientConfig 客户端配置
type ClientConfig struct {
	ID   string `yaml:"id"`   // 客户端标识
	Name string `yaml:"name"` // 客户端名称
}

// LogConfig 日志配置
type LogConfig struct {
	Level string `yaml:"level"` // 日志级别
	File  string `yaml:"file"`  // 日志文件路径
}

var (
	cfg *Config
)

// Load 加载配置文件
func Load(configPath string) (*Config, error) {
	viper.SetConfigType("yaml")

	// 设置默认值
	setDefaults()

	if configPath != "" {
		viper.SetConfigFile(configPath)
	} else {
		// 默认查找配置文件的位置
		viper.SetConfigName("config")
		viper.AddConfigPath("./configs")
		viper.AddConfigPath(".")
	}

	// 支持环境变量
	viper.AutomaticEnv()
	viper.SetEnvPrefix("CLAUDE")

	// 读取配置文件
	if err := viper.ReadInConfig(); err != nil {
		return nil, fmt.Errorf("读取配置文件失败: %v", err)
	}

	// 解析配置
	cfg = &Config{}
	if err := viper.Unmarshal(cfg); err != nil {
		return nil, fmt.Errorf("解析配置失败: %v", err)
	}

	// 验证配置
	if err := validate(cfg); err != nil {
		return nil, fmt.Errorf("配置验证失败: %v", err)
	}

	return cfg, nil
}

// setDefaults 设置默认配置
func setDefaults() {
	// 服务器默认配置
	viper.SetDefault("server.url", "ws://localhost:8080/ws/connector")
	viper.SetDefault("server.token", "")

	// Claude 默认配置
	viper.SetDefault("claude.command", "claude")
	viper.SetDefault("claude.timeout", 300)

	// 客户端默认配置
	viper.SetDefault("client.id", "connector-001")
	viper.SetDefault("client.name", "Claude Connector")

	// 日志默认配置
	viper.SetDefault("log.level", "info")
	viper.SetDefault("log.file", filepath.Join("logs", "claude-connector.log"))
}

// validate 验证配置
func validate(cfg *Config) error {
	if cfg.Server.URL == "" {
		return fmt.Errorf("服务器地址不能为空")
	}

	if cfg.Server.Token == "" {
		return fmt.Errorf("认证令牌不能为空")
	}

	if cfg.Claude.Timeout <= 0 {
		return fmt.Errorf("超时时间必须大于0")
	}

	return nil
}

// Get 获取配置
func Get() *Config {
	return cfg
}
