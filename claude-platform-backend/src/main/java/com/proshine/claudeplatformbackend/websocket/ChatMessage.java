package com.proshine.claudeplatformbackend.websocket;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    
    private String id;
    private String type;
    private String userId;
    private String clientType; // "user" 或 "client"
    private String content;
    private Map<String, Object> data;
    private Long timestamp;
    
    // 消息类型常量
    public static final String TYPE_AUTH = "auth";
    public static final String TYPE_HEARTBEAT = "heartbeat";
    public static final String TYPE_CHAT_REQUEST = "chat_request";
    public static final String TYPE_CHAT_RESPONSE = "chat_response";
    public static final String TYPE_ERROR = "error";
    public static final String TYPE_SUCCESS = "success";
    
    // 客户端类型常量
    public static final String CLIENT_TYPE_USER = "user";
    public static final String CLIENT_TYPE_CLIENT = "client";
    
    public static ChatMessage auth(String userId, String clientType) {
        ChatMessage message = new ChatMessage();
        message.setId(java.util.UUID.randomUUID().toString());
        message.setType(TYPE_AUTH);
        message.setUserId(userId);
        message.setClientType(clientType);
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }
    
    public static ChatMessage heartbeat(String userId) {
        ChatMessage message = new ChatMessage();
        message.setId(java.util.UUID.randomUUID().toString());
        message.setType(TYPE_HEARTBEAT);
        message.setUserId(userId);
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }
    
    public static ChatMessage chatRequest(String userId, String content, Map<String, Object> data) {
        ChatMessage message = new ChatMessage();
        message.setId(java.util.UUID.randomUUID().toString());
        message.setType(TYPE_CHAT_REQUEST);
        message.setUserId(userId);
        message.setContent(content);
        message.setData(data);
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }
    
    public static ChatMessage chatResponse(String id, String content, Map<String, Object> data) {
        ChatMessage message = new ChatMessage();
        message.setId(id);
        message.setType(TYPE_CHAT_RESPONSE);
        message.setContent(content);
        message.setData(data);
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }
    
    public static ChatMessage error(String id, String error) {
        ChatMessage message = new ChatMessage();
        message.setId(id != null ? id : java.util.UUID.randomUUID().toString());
        message.setType(TYPE_ERROR);
        message.setContent(error);
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }
    
    public static ChatMessage success(String id, String content) {
        ChatMessage message = new ChatMessage();
        message.setId(id != null ? id : java.util.UUID.randomUUID().toString());
        message.setType(TYPE_SUCCESS);
        message.setContent(content);
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }
}