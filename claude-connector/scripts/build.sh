#!/bin/bash

echo "Building Claude Connector..."

# 创建输出目录
mkdir -p dist

# 设置版本信息
VERSION="1.0.0"
BUILD_TIME=$(date +"%Y-%m-%d %H:%M:%S")

# Windows 64位
echo "Building Windows amd64..."
GOOS=windows GOARCH=amd64 go build -ldflags "-X main.version=$VERSION" -o dist/claude-connector-windows-amd64.exe ./cmd/main.go

# Windows 32位
echo "Building Windows 386..."
GOOS=windows GOARCH=386 go build -ldflags "-X main.version=$VERSION" -o dist/claude-connector-windows-386.exe ./cmd/main.go

# Linux 64位
echo "Building Linux amd64..."
GOOS=linux GOARCH=amd64 go build -ldflags "-X main.version=$VERSION" -o dist/claude-connector-linux-amd64 ./cmd/main.go

# macOS Intel
echo "Building macOS amd64..."
GOOS=darwin GOARCH=amd64 go build -ldflags "-X main.version=$VERSION" -o dist/claude-connector-darwin-amd64 ./cmd/main.go

# macOS M1/M2
echo "Building macOS arm64..."
GOOS=darwin GOARCH=arm64 go build -ldflags "-X main.version=$VERSION" -o dist/claude-connector-darwin-arm64 ./cmd/main.go

# 复制配置文件
echo "Copying config file..."
cp configs/config.yaml dist/config.yaml.example

# 设置 Linux/Mac 可执行权限
chmod +x dist/claude-connector-linux-amd64
chmod +x dist/claude-connector-darwin-amd64
chmod +x dist/claude-connector-darwin-arm64

echo "Build completed!"
echo "Output files in dist/ directory"