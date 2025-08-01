package ws

import (
	"encoding/json"
	"fmt"
	"net/http"
	"sync"
	"time"

	"claude-connector/internal/config"
	"claude-connector/internal/models"

	"github.com/gorilla/websocket"
	"github.com/sirupsen/logrus"
)

// Client WebSocket 客户端
type Client struct {
	config               *config.Config
	conn                 *websocket.Conn
	logger               *logrus.Logger
	sendChan             chan []byte
	receiveChan          chan *models.Request
	doneChan             chan struct{}
	mu                   sync.Mutex
	isConnected          bool
	reconnectInterval    time.Duration
	maxReconnectInterval time.Duration
}

// New 创建新的 WebSocket 客户端
func New(cfg *config.Config, logger *logrus.Logger) *Client {
	return &Client{
		config:               cfg,
		logger:               logger,
		sendChan:             make(chan []byte, 256),
		receiveChan:          make(chan *models.Request, 256),
		doneChan:             make(chan struct{}),
		reconnectInterval:    5 * time.Second,
		maxReconnectInterval: 5 * time.Minute,
	}
}

// Connect 连接到服务器
func (c *Client) Connect() error {
	c.mu.Lock()
	defer c.mu.Unlock()

	if c.isConnected {
		return nil
	}

	c.logger.Infof("连接到服务器: %s", c.config.Server.URL)

	// 设置请求头
	header := http.Header{}
	header.Set("Authorization", "Bearer "+c.config.Server.Token)

	// 建立连接
	dialer := websocket.Dialer{
		HandshakeTimeout: 10 * time.Second,
	}

	conn, _, err := dialer.Dial(c.config.Server.URL, header)
	if err != nil {
		return fmt.Errorf("连接失败: %v", err)
	}

	c.conn = conn
	c.isConnected = true

	// 发送认证消息
	if err := c.authenticate(); err != nil {
		c.conn.Close()
		c.isConnected = false
		return fmt.Errorf("认证失败: %v", err)
	}

	// 启动读写协程
	go c.readLoop()
	go c.writeLoop()
	go c.pingLoop()

	c.logger.Info("WebSocket 连接成功")
	return nil
}

// authenticate 发送认证消息
func (c *Client) authenticate() error {
	auth := models.AuthMessage{
		Token:      c.config.Server.Token,
		ClientID:   c.config.Client.ID,
		ClientName: c.config.Client.Name,
		ClientType: "connector",
	}

	data, err := json.Marshal(auth)
	if err != nil {
		return err
	}

	return c.conn.WriteMessage(websocket.TextMessage, data)
}

// readLoop 读取消息循环
func (c *Client) readLoop() {
	defer func() {
		c.handleDisconnect()
	}()

	c.conn.SetReadDeadline(time.Now().Add(60 * time.Second))
	c.conn.SetPongHandler(func(string) error {
		c.conn.SetReadDeadline(time.Now().Add(60 * time.Second))
		return nil
	})

	for {
		_, message, err := c.conn.ReadMessage()
		if err != nil {
			if websocket.IsUnexpectedCloseError(err, websocket.CloseGoingAway, websocket.CloseAbnormalClosure) {
				c.logger.Errorf("读取消息错误: %v", err)
			}
			return
		}

		// 解析消息
		var request models.Request
		if err := json.Unmarshal(message, &request); err != nil {
			c.logger.Errorf("解析消息失败: %v", err)
			continue
		}

		c.logger.Debugf("收到消息: type=%s, id=%s", request.Type, request.ID)

		// 处理不同类型的消息
		switch request.Type {
		case models.TypePing:
			// 响应心跳
			c.sendPong()
		case models.TypeExecute, models.TypeUpload:
			// 发送到处理通道
			select {
			case c.receiveChan <- &request:
			default:
				c.logger.Warn("接收通道已满，丢弃消息")
			}
		}
	}
}

// writeLoop 写入消息循环
func (c *Client) writeLoop() {
	ticker := time.NewTicker(54 * time.Second)
	defer func() {
		ticker.Stop()
		c.handleDisconnect()
	}()

	for {
		select {
		case message, ok := <-c.sendChan:
			c.conn.SetWriteDeadline(time.Now().Add(10 * time.Second))
			if !ok {
				c.conn.WriteMessage(websocket.CloseMessage, []byte{})
				return
			}

			if err := c.conn.WriteMessage(websocket.TextMessage, message); err != nil {
				c.logger.Errorf("发送消息失败: %v", err)
				return
			}

		case <-ticker.C:
			c.conn.SetWriteDeadline(time.Now().Add(10 * time.Second))
			if err := c.conn.WriteMessage(websocket.PingMessage, nil); err != nil {
				return
			}

		case <-c.doneChan:
			return
		}
	}
}

// pingLoop 定期发送心跳
func (c *Client) pingLoop() {
	ticker := time.NewTicker(30 * time.Second)
	defer ticker.Stop()

	for {
		select {
		case <-ticker.C:
			c.sendPing()
		case <-c.doneChan:
			return
		}
	}
}

// sendPing 发送心跳消息
func (c *Client) sendPing() {
	ping := models.NewRequest(models.TypePing)
	c.Send(ping)
}

// sendPong 发送心跳响应
func (c *Client) sendPong() {
	pong := models.NewRequest(models.TypePong)
	c.Send(pong)
}

// Send 发送消息
func (c *Client) Send(message interface{}) error {
	c.mu.Lock()
	if !c.isConnected {
		c.mu.Unlock()
		return fmt.Errorf("未连接到服务器")
	}
	c.mu.Unlock()

	data, err := json.Marshal(message)
	if err != nil {
		return fmt.Errorf("序列化消息失败: %v", err)
	}

	select {
	case c.sendChan <- data:
		return nil
	case <-time.After(5 * time.Second):
		return fmt.Errorf("发送超时")
	}
}

// Receive 接收消息
func (c *Client) Receive() <-chan *models.Request {
	return c.receiveChan
}

// handleDisconnect 处理断开连接
func (c *Client) handleDisconnect() {
	c.mu.Lock()
	if !c.isConnected {
		c.mu.Unlock()
		return
	}

	c.isConnected = false
	c.conn.Close()
	c.mu.Unlock()

	c.logger.Warn("WebSocket 连接断开")

	// 触发重连
	go c.reconnectLoop()
}

// reconnectLoop 重连循环
func (c *Client) reconnectLoop() {
	interval := c.reconnectInterval

	for {
		select {
		case <-c.doneChan:
			return
		default:
		}

		c.logger.Infof("尝试重连，等待 %v", interval)
		time.Sleep(interval)

		if err := c.Connect(); err != nil {
			c.logger.Errorf("重连失败: %v", err)
			// 指数退避
			interval = interval * 2
			if interval > c.maxReconnectInterval {
				interval = c.maxReconnectInterval
			}
		} else {
			c.logger.Info("重连成功")
			return
		}
	}
}

// Close 关闭连接
func (c *Client) Close() error {
	c.mu.Lock()
	defer c.mu.Unlock()

	if !c.isConnected {
		return nil
	}

	close(c.doneChan)
	close(c.sendChan)

	// 发送关闭消息
	c.conn.WriteMessage(websocket.CloseMessage, websocket.FormatCloseMessage(websocket.CloseNormalClosure, ""))

	return c.conn.Close()
}

// IsConnected 检查是否已连接
func (c *Client) IsConnected() bool {
	c.mu.Lock()
	defer c.mu.Unlock()
	return c.isConnected
}
