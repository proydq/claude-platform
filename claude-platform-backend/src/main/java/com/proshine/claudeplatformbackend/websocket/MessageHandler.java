package com.proshine.claudeplatformbackend.websocket;

import com.proshine.claudeplatformbackend.security.JwtTokenProvider;
import com.proshine.claudeplatformbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

@Component
public class MessageHandler {
    
    @Autowired
    private ConnectionManager connectionManager;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private UserService userService;
    
    public void handleMessage(WebSocketSession session, ChatMessage message) {
        try {
            switch (message.getType()) {
                case ChatMessage.TYPE_AUTH:
                    handleAuthMessage(session, message);
                    break;
                case ChatMessage.TYPE_HEARTBEAT:
                    handleHeartbeatMessage(session, message);
                    break;
                case ChatMessage.TYPE_CHAT_REQUEST:
                    handleChatRequestMessage(session, message);
                    break;
                case ChatMessage.TYPE_CHAT_RESPONSE:
                    handleChatResponseMessage(session, message);
                    break;
                default:
                    ChatMessage errorMessage = ChatMessage.error(message.getId(), "未知的消息类型: " + message.getType());
                    connectionManager.sendMessageToSession(session, errorMessage);
                    break;
            }
        } catch (Exception e) {
            System.err.println("处理WebSocket消息失败: " + e.getMessage());
            ChatMessage errorMessage = ChatMessage.error(message.getId(), "处理消息失败: " + e.getMessage());
            connectionManager.sendMessageToSession(session, errorMessage);
        }
    }
    
    private void handleAuthMessage(WebSocketSession session, ChatMessage message) {
        try {
            String clientType = message.getClientType();
            
            if (ChatMessage.CLIENT_TYPE_USER.equals(clientType)) {
                // 用户认证
                String username = (String) session.getAttributes().get("username");
                if (username != null) {
                    connectionManager.addUserSession(username, session);
                    
                    ChatMessage response = ChatMessage.success(message.getId(), "用户认证成功");
                    connectionManager.sendMessageToSession(session, response);
                } else {
                    ChatMessage response = ChatMessage.error(message.getId(), "用户认证失败，无效的token");
                    connectionManager.sendMessageToSession(session, response);
                }
            } else if (ChatMessage.CLIENT_TYPE_CLIENT.equals(clientType)) {
                // 本地客户端认证
                connectionManager.addClientSession(session);
                
                ChatMessage response = ChatMessage.success(message.getId(), "客户端认证成功");
                connectionManager.sendMessageToSession(session, response);
            } else {
                ChatMessage response = ChatMessage.error(message.getId(), "未知的客户端类型: " + clientType);
                connectionManager.sendMessageToSession(session, response);
            }
        } catch (Exception e) {
            ChatMessage response = ChatMessage.error(message.getId(), "认证失败: " + e.getMessage());
            connectionManager.sendMessageToSession(session, response);
        }
    }
    
    private void handleHeartbeatMessage(WebSocketSession session, ChatMessage message) {
        // 更新会话的最后活跃时间
        session.getAttributes().put("lastHeartbeat", System.currentTimeMillis());
        
        // 回复心跳响应
        ChatMessage response = ChatMessage.heartbeat(message.getUserId());
        response.setId(message.getId());
        connectionManager.sendMessageToSession(session, response);
    }
    
    private void handleChatRequestMessage(WebSocketSession session, ChatMessage message) {
        String sessionType = connectionManager.getSessionType(session);
        
        if (ChatMessage.CLIENT_TYPE_USER.equals(sessionType)) {
            // 用户发送的聊天请求，转发给本地客户端
            if (connectionManager.hasClientConnections()) {
                connectionManager.sendMessageToClients(message);
            } else {
                ChatMessage response = ChatMessage.error(message.getId(), "暂无可用的本地客户端");
                connectionManager.sendMessageToSession(session, response);
            }
        } else {
            ChatMessage response = ChatMessage.error(message.getId(), "只有用户可以发送聊天请求");
            connectionManager.sendMessageToSession(session, response);
        }
    }
    
    private void handleChatResponseMessage(WebSocketSession session, ChatMessage message) {
        String sessionType = connectionManager.getSessionType(session);
        
        if (ChatMessage.CLIENT_TYPE_CLIENT.equals(sessionType)) {
            // 本地客户端发送的响应，转发给相应的用户
            String userId = message.getUserId();
            if (userId != null) {
                connectionManager.sendMessageToUser(userId, message);
            } else {
                // 如果没有指定用户ID，广播给所有用户（一般不应该发生）
                connectionManager.broadcastToAllUsers(message);
            }
        } else {
            ChatMessage response = ChatMessage.error(message.getId(), "只有本地客户端可以发送聊天响应");
            connectionManager.sendMessageToSession(session, response);
        }
    }
    
    public void sendChatRequestToClient(String userId, String content, Map<String, Object> data) {
        if (connectionManager.hasClientConnections()) {
            ChatMessage message = ChatMessage.chatRequest(userId, content, data);
            connectionManager.sendMessageToClients(message);
        } else {
            System.err.println("无可用的本地客户端来处理聊天请求");
        }
    }
    
    public void sendChatResponseToUser(String userId, String messageId, String content, Map<String, Object> data) {
        ChatMessage message = ChatMessage.chatResponse(messageId, content, data);
        message.setUserId(userId);
        connectionManager.sendMessageToUser(userId, message);
    }
    
    public boolean hasAvailableClients() {
        return connectionManager.hasClientConnections();
    }
    
    public Map<String, Object> getConnectionStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("userConnections", connectionManager.getUserConnectionCount());
        status.put("clientConnections", connectionManager.getClientConnectionCount());
        status.put("hasClients", connectionManager.hasClientConnections());
        return status;
    }
}