package models

import "time"

// MessageType 消息类型
type MessageType string

const (
	TypeAuth     MessageType = "auth"     // 认证
	TypeExecute  MessageType = "execute"  // 执行命令
	TypeUpload   MessageType = "upload"   // 上传文件
	TypePing     MessageType = "ping"     // 心跳
	TypePong     MessageType = "pong"     // 心跳响应
	TypeResponse MessageType = "response" // 命令响应
)

// Request 请求消息
type Request struct {
	ID        string            `json:"id"`
	Type      MessageType       `json:"type"`
	Command   string            `json:"command,omitempty"`
	Files     map[string]string `json:"files,omitempty"` // 文件名:内容(base64)
	WorkDir   string            `json:"workDir,omitempty"`
	Timestamp int64             `json:"timestamp"`
}

// Response 响应消息
type Response struct {
	ID        string      `json:"id"`
	Type      MessageType `json:"type"`
	Success   bool        `json:"success"`
	Output    string      `json:"output,omitempty"`
	Error     string      `json:"error,omitempty"`
	Timestamp int64       `json:"timestamp"`
}

// AuthMessage 认证消息
type AuthMessage struct {
	Token      string `json:"token"`
	ClientID   string `json:"clientId"`
	ClientName string `json:"clientName"`
	ClientType string `json:"clientType"` // "connector"
}

// NewRequest 创建新的请求
func NewRequest(msgType MessageType) *Request {
	return &Request{
		Type:      msgType,
		Timestamp: time.Now().Unix(),
	}
}

// NewResponse 创建新的响应
func NewResponse(requestID string, success bool) *Response {
	return &Response{
		ID:        requestID,
		Type:      TypeResponse,
		Success:   success,
		Timestamp: time.Now().Unix(),
	}
}
