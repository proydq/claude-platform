package com.proshine.claudeplatformbackend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class ConnectionManager {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 用户连接映射 userId -> sessions
    private final Map<String, CopyOnWriteArraySet<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    
    // 本地客户端连接
    private final CopyOnWriteArraySet<WebSocketSession> clientSessions = new CopyOnWriteArraySet<>();
    
    // session到用户的映射
    private final Map<WebSocketSession, String> sessionToUser = new ConcurrentHashMap<>();
    
    // session类型映射
    private final Map<WebSocketSession, String> sessionTypes = new ConcurrentHashMap<>();
    
    public void addUserSession(String userId, WebSocketSession session) {
        userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
        sessionToUser.put(session, userId);
        sessionTypes.put(session, ChatMessage.CLIENT_TYPE_USER);
        
        System.out.println("用户连接: " + userId + ", 会话ID: " + session.getId());
    }
    
    public void addClientSession(WebSocketSession session) {
        clientSessions.add(session);
        sessionTypes.put(session, ChatMessage.CLIENT_TYPE_CLIENT);
        
        System.out.println("本地客户端连接: " + session.getId());
    }
    
    public void removeSession(WebSocketSession session) {
        String userId = sessionToUser.remove(session);
        String sessionType = sessionTypes.remove(session);
        
        if (ChatMessage.CLIENT_TYPE_USER.equals(sessionType) && userId != null) {
            CopyOnWriteArraySet<WebSocketSession> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
            System.out.println("用户断开连接: " + userId + ", 会话ID: " + session.getId());
        } else if (ChatMessage.CLIENT_TYPE_CLIENT.equals(sessionType)) {
            clientSessions.remove(session);
            System.out.println("本地客户端断开连接: " + session.getId());
        }
    }
    
    public void sendMessageToUser(String userId, ChatMessage message) {
        CopyOnWriteArraySet<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null) {
            String messageJson = convertToJson(message);
            if (messageJson != null) {
                sessions.forEach(session -> sendMessage(session, messageJson));
            }
        }
    }
    
    public void sendMessageToClients(ChatMessage message) {
        String messageJson = convertToJson(message);
        if (messageJson != null) {
            clientSessions.forEach(session -> sendMessage(session, messageJson));
        }
    }
    
    public void sendMessageToSession(WebSocketSession session, ChatMessage message) {
        String messageJson = convertToJson(message);
        if (messageJson != null) {
            sendMessage(session, messageJson);
        }
    }
    
    public void broadcastToAllUsers(ChatMessage message) {
        String messageJson = convertToJson(message);
        if (messageJson != null) {
            userSessions.values().forEach(sessions -> 
                sessions.forEach(session -> sendMessage(session, messageJson))
            );
        }
    }
    
    private void sendMessage(WebSocketSession session, String message) {
        if (session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                System.err.println("发送WebSocket消息失败: " + e.getMessage());
                // 如果发送失败，可能连接已断开，移除该session
                removeSession(session);
            }
        }
    }
    
    private String convertToJson(ChatMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            System.err.println("转换WebSocket消息为JSON失败: " + e.getMessage());
            return null;
        }
    }
    
    public int getUserConnectionCount() {
        return userSessions.size();
    }
    
    public int getClientConnectionCount() {
        return clientSessions.size();
    }
    
    public boolean hasUserConnections(String userId) {
        CopyOnWriteArraySet<WebSocketSession> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }
    
    public boolean hasClientConnections() {
        return !clientSessions.isEmpty();
    }
    
    public String getUserIdBySession(WebSocketSession session) {
        return sessionToUser.get(session);
    }
    
    public String getSessionType(WebSocketSession session) {
        return sessionTypes.get(session);
    }
}