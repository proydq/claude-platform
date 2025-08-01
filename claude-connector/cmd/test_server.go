package main

import (
	"encoding/json"
	"log"
	"net/http"
	"time"

	"claude-connector/internal/models"
	"github.com/gorilla/websocket"
)

// 测试服务器
type TestServer struct {
	upgrader websocket.Upgrader
}

func NewTestServer() *TestServer {
	return &TestServer{
		upgrader: websocket.Upgrader{
			CheckOrigin: func(r *http.Request) bool {
				return true // 允许所有来源
			},
		},
	}
}

// 处理 WebSocket 连接
func (s *TestServer) handleWebSocket(w http.ResponseWriter, r *http.Request) {
	// 升级为 WebSocket 连接
	conn, err := s.upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Printf("升级连接失败: %v", err)
		return
	}
	defer conn.Close()

	log.Printf("新客户端连接: %s", conn.RemoteAddr())

	// 读取认证消息
	_, message, err := conn.ReadMessage()
	if err != nil {
		log.Printf("读取认证消息失败: %v", err)
		return
	}

	var auth models.AuthMessage
	if err := json.Unmarshal(message, &auth); err != nil {
		log.Printf("解析认证消息失败: %v", err)
		return
	}

	log.Printf("客户端认证成功: ID=%s, Name=%s", auth.ClientID, auth.ClientName)

	// 启动一个协程定期发送测试命令
	go func() {
		time.Sleep(5 * time.Second)

		// 发送一个测试命令
		testRequest := &models.Request{
			ID:        "test-001",
			Type:      models.TypeExecute,
			Command:   "echo Hello from test server!",
			Timestamp: time.Now().Unix(),
		}

		data, _ := json.Marshal(testRequest)
		if err := conn.WriteMessage(websocket.TextMessage, data); err != nil {
			log.Printf("发送测试命令失败: %v", err)
			return
		}
		log.Println("已发送测试命令")
	}()

	// 读取消息循环
	for {
		_, message, err := conn.ReadMessage()
		if err != nil {
			if websocket.IsUnexpectedCloseError(err, websocket.CloseGoingAway, websocket.CloseAbnormalClosure) {
				log.Printf("连接异常关闭: %v", err)
			}
			break
		}

		// 尝试解析为响应消息
		var response models.Response
		if err := json.Unmarshal(message, &response); err == nil && response.Type == models.TypeResponse {
			log.Printf("收到响应: ID=%s, Success=%v, Output=%s",
				response.ID, response.Success, response.Output)

			// 5秒后发送另一个命令
			time.Sleep(5 * time.Second)

			// 发送带文件的命令
			fileRequest := &models.Request{
				ID:      "test-002",
				Type:    models.TypeExecute,
				Command: "dir",
				Files: map[string]string{
					"test.txt": "SGVsbG8gV29ybGQh", // "Hello World!" 的 base64
				},
				Timestamp: time.Now().Unix(),
			}

			data, _ := json.Marshal(fileRequest)
			if err := conn.WriteMessage(websocket.TextMessage, data); err != nil {
				log.Printf("发送文件命令失败: %v", err)
				break
			}
			log.Println("已发送带文件的命令")
			continue
		}

		// 处理心跳
		var request models.Request
		if err := json.Unmarshal(message, &request); err == nil {
			if request.Type == models.TypePing || request.Type == models.TypePong {
				log.Printf("收到心跳: %s", request.Type)
			}
		}
	}

	log.Printf("客户端断开连接: %s", conn.RemoteAddr())
}

func main() {
	server := NewTestServer()

	// 注册路由
	http.HandleFunc("/ws/connector", server.handleWebSocket)

	// 添加一个简单的健康检查接口
	http.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		w.Write([]byte("OK"))
	})

	log.Println("测试 WebSocket 服务器启动在 :8080")
	log.Println("WebSocket 地址: ws://localhost:8080/ws/connector")

	if err := http.ListenAndServe(":8080", nil); err != nil {
		log.Fatalf("服务器启动失败: %v", err)
	}
}
