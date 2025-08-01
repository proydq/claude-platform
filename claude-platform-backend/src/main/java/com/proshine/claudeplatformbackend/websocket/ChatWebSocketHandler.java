package com.proshine.claudeplatformbackend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proshine.claudeplatformbackend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Component
public class ChatWebSocketHandler implements WebSocketHandler {
    
    @Autowired
    private ConnectionManager connectionManager;
    
    @Autowired
    private MessageHandler messageHandler;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("WebSocket连接建立: " + session.getId());
        
        // 从URL参数获取token
        String token = getTokenFromSession(session);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            System.out.println("用户通过Token认证: " + username);
            
            // 暂时将用户添加到连接管理器，等待进一步的认证消息
            session.getAttributes().put("username", username);
        }
        
        // 发送欢迎消息
        ChatMessage welcomeMessage = ChatMessage.success(null, "WebSocket连接已建立，请发送认证消息");
        connectionManager.sendMessageToSession(session, welcomeMessage);
    }
    
    @Override
    public void handleMessage(WebSocketSession session, org.springframework.web.socket.WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            handleTextMessage(session, textMessage);
        }
    }
    
    private void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ChatMessage wsMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);
            messageHandler.handleMessage(session, wsMessage);
        } catch (Exception e) {
            System.err.println("处理WebSocket消息失败: " + e.getMessage());
            ChatMessage errorMessage = ChatMessage.error(null, "消息格式错误: " + e.getMessage());
            connectionManager.sendMessageToSession(session, errorMessage);
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket传输错误: " + session.getId() + ", " + exception.getMessage());
        connectionManager.removeSession(session);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        System.out.println("WebSocket连接关闭: " + session.getId() + ", 状态: " + closeStatus.toString());
        connectionManager.removeSession(session);
    }
    
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    
    private String getTokenFromSession(WebSocketSession session) {
        try {
            URI uri = session.getUri();
            if (uri != null && uri.getQuery() != null) {
                String query = uri.getQuery();
                String[] params = query.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2 && "token".equals(keyValue[0])) {
                        return keyValue[1];
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("从WebSocket URL获取token失败: " + e.getMessage());
        }
        return null;
    }
}