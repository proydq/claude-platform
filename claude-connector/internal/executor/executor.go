package executor

import (
	"context"
	"encoding/base64"
	"fmt"
	"io"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"time"

	"github.com/sirupsen/logrus"
)

// Executor Claude 执行器
type Executor struct {
	command string        // Claude 命令
	timeout time.Duration // 执行超时时间
	tempDir string        // 临时文件目录
	logger  *logrus.Logger
}

// New 创建新的执行器
func New(command string, timeout int, logger *logrus.Logger) (*Executor, error) {
	// 验证 Claude 命令是否可用
	if _, err := exec.LookPath(command); err != nil {
		return nil, fmt.Errorf("找不到 Claude 命令 '%s': %v", command, err)
	}

	// 创建临时目录
	tempDir := filepath.Join(".", "temp")
	if err := os.MkdirAll(tempDir, 0755); err != nil {
		return nil, fmt.Errorf("创建临时目录失败: %v", err)
	}

	return &Executor{
		command: command,
		timeout: time.Duration(timeout) * time.Second,
		tempDir: tempDir,
		logger:  logger,
	}, nil
}

// Execute 执行 Claude 命令
func (e *Executor) Execute(command string, workDir string, files map[string]string) (string, error) {
	e.logger.Infof("执行命令: %s", command)

	// 处理工作目录
	if workDir == "" {
		workDir = "."
	}

	// 保存文件到临时目录
	tempFiles := []string{}
	if len(files) > 0 {
		for filename, content := range files {
			filepath, err := e.saveFile(filename, content)
			if err != nil {
				return "", fmt.Errorf("保存文件失败: %v", err)
			}
			tempFiles = append(tempFiles, filepath)
			e.logger.Debugf("保存临时文件: %s", filepath)
		}
	}

	// 创建上下文用于超时控制
	ctx, cancel := context.WithTimeout(context.Background(), e.timeout)
	defer cancel()

	// 构建命令
	args := strings.Fields(command)
	cmd := exec.CommandContext(ctx, e.command, args...)
	cmd.Dir = workDir

	// 设置环境变量
	cmd.Env = os.Environ()

	// 创建管道
	stdout, err := cmd.StdoutPipe()
	if err != nil {
		return "", fmt.Errorf("创建 stdout 管道失败: %v", err)
	}

	stderr, err := cmd.StderrPipe()
	if err != nil {
		return "", fmt.Errorf("创建 stderr 管道失败: %v", err)
	}

	// 启动命令
	if err := cmd.Start(); err != nil {
		return "", fmt.Errorf("启动命令失败: %v", err)
	}

	// 读取输出
	outputChan := make(chan string, 1)
	errorChan := make(chan string, 1)

	go func() {
		output, _ := io.ReadAll(stdout)
		outputChan <- string(output)
	}()

	go func() {
		errOutput, _ := io.ReadAll(stderr)
		errorChan <- string(errOutput)
	}()

	// 等待命令完成
	err = cmd.Wait()

	// 获取输出
	output := <-outputChan
	errOutput := <-errorChan

	// 清理临时文件
	for _, filepath := range tempFiles {
		if err := os.Remove(filepath); err != nil {
			e.logger.Warnf("删除临时文件失败: %s, %v", filepath, err)
		}
	}

	// 处理错误
	if ctx.Err() == context.DeadlineExceeded {
		return "", fmt.Errorf("命令执行超时 (%v)", e.timeout)
	}

	if err != nil {
		if errOutput != "" {
			return "", fmt.Errorf("命令执行失败: %s", errOutput)
		}
		return "", fmt.Errorf("命令执行失败: %v", err)
	}

	// 合并输出
	result := output
	if errOutput != "" {
		result = result + "\n" + errOutput
	}

	e.logger.Debugf("命令执行完成，输出长度: %d", len(result))
	return strings.TrimSpace(result), nil
}

// saveFile 保存文件到临时目录
func (e *Executor) saveFile(filename string, content string) (string, error) {
	// 解码 base64 内容
	data, err := base64.StdEncoding.DecodeString(content)
	if err != nil {
		return "", fmt.Errorf("解码文件内容失败: %v", err)
	}

	// 生成安全的文件名
	safeFilename := fmt.Sprintf("%d_%s", time.Now().UnixNano(), filepath.Base(filename))
	filepath := filepath.Join(e.tempDir, safeFilename)

	// 写入文件
	if err := os.WriteFile(filepath, data, 0644); err != nil {
		return "", fmt.Errorf("写入文件失败: %v", err)
	}

	return filepath, nil
}

// CleanupOldFiles 清理旧的临时文件
func (e *Executor) CleanupOldFiles(maxAge time.Duration) error {
	entries, err := os.ReadDir(e.tempDir)
	if err != nil {
		return fmt.Errorf("读取临时目录失败: %v", err)
	}

	now := time.Now()
	for _, entry := range entries {
		if entry.IsDir() {
			continue
		}

		info, err := entry.Info()
		if err != nil {
			continue
		}

		if now.Sub(info.ModTime()) > maxAge {
			filepath := filepath.Join(e.tempDir, entry.Name())
			if err := os.Remove(filepath); err != nil {
				e.logger.Warnf("删除过期文件失败: %s, %v", filepath, err)
			} else {
				e.logger.Debugf("删除过期文件: %s", filepath)
			}
		}
	}

	return nil
}
