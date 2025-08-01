@echo off
echo Building Claude Connector...

REM 创建输出目录
if not exist "dist" mkdir dist

REM 设置版本信息
set VERSION=1.0.0
set BUILD_TIME=%date% %time%

REM Windows 64位
echo Building Windows amd64...
set GOOS=windows
set GOARCH=amd64
go build -ldflags "-X main.version=%VERSION%" -o dist/claude-connector-windows-amd64.exe ./cmd/main.go

REM Windows 32位
echo Building Windows 386...
set GOOS=windows
set GOARCH=386
go build -ldflags "-X main.version=%VERSION%" -o dist/claude-connector-windows-386.exe ./cmd/main.go

REM Linux 64位
echo Building Linux amd64...
set GOOS=linux
set GOARCH=amd64
go build -ldflags "-X main.version=%VERSION%" -o dist/claude-connector-linux-amd64 ./cmd/main.go

REM macOS Intel
echo Building macOS amd64...
set GOOS=darwin
set GOARCH=amd64
go build -ldflags "-X main.version=%VERSION%" -o dist/claude-connector-darwin-amd64 ./cmd/main.go

REM macOS M1/M2
echo Building macOS arm64...
set GOOS=darwin
set GOARCH=arm64
go build -ldflags "-X main.version=%VERSION%" -o dist/claude-connector-darwin-arm64 ./cmd/main.go

REM 复制配置文件
echo Copying config file...
copy configs\config.yaml dist\config.yaml.example

echo Build completed!
echo Output files in dist/ directory
pause