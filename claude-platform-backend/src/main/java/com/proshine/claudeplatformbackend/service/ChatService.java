package com.proshine.claudeplatformbackend.service;

import com.proshine.claudeplatformbackend.dto.request.ChatRequest;
import com.proshine.claudeplatformbackend.dto.response.ChatResponse;
import com.proshine.claudeplatformbackend.entity.Conversation;
import com.proshine.claudeplatformbackend.entity.FileRecord;
import com.proshine.claudeplatformbackend.security.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {
    
    @Autowired
    private ConversationService conversationService;
    
    @Autowired
    private FileService fileService;
    
    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private SystemService systemService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Transactional
    public ChatResponse processChat(ChatRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ChatResponse.error(null, "用户未登录");
        }
        
        try {
            // 检查Token额度
            Integer maxTokensPerRequest = systemService.getMaxTokensPerRequest();
            if (!tokenService.checkTokenAvailable(userId, maxTokensPerRequest)) {
                return ChatResponse.error(request.getConversationId(), "Token额度不足");
            }
            
            // 处理文件内容
            List<String> fileContents = new ArrayList<>();
            if (request.getFileIds() != null && !request.getFileIds().isEmpty()) {
                for (String fileId : request.getFileIds()) {
                    try {
                        if (fileService.fileExists(fileId)) {
                            String content = fileService.getFileContent(fileId);
                            FileRecord fileRecord = fileService.getFileById(fileId);
                            fileContents.add("文件: " + fileRecord.getFileName() + "\n内容:\n" + content);
                        }
                    } catch (Exception e) {
                        System.err.println("读取文件失败: " + fileId + ", " + e.getMessage());
                    }
                }
            }
            
            // 构建完整的消息内容
            StringBuilder fullMessage = new StringBuilder();
            fullMessage.append(request.getMessage());
            
            if (!fileContents.isEmpty()) {
                fullMessage.append("\n\n附件内容:\n");
                for (String fileContent : fileContents) {
                    fullMessage.append(fileContent).append("\n\n");
                }
            }
            
            // 这里应该通过WebSocket发送给本地客户端
            // 目前先模拟响应
            String response = simulateClaudeResponse(fullMessage.toString());
            Integer tokensUsed = estimateTokenUsage(fullMessage.toString(), response);
            
            // 创建或更新对话
            String conversationId = request.getConversationId();
            if (conversationId == null || conversationId.isEmpty()) {
                // 创建新对话
                String title = request.getTitle();
                if (title == null || title.isEmpty()) {
                    title = generateConversationTitle(request.getMessage());
                }
                
                String conversationContent = buildConversationContent(request.getMessage(), response, fileContents);
                Conversation conversation = conversationService.createConversation(title, conversationContent, tokensUsed);
                conversationId = conversation.getId();
            } else {
                // 更新现有对话
                Conversation existingConversation = conversationService.getConversationById(conversationId);
                String updatedContent = appendToConversationContent(existingConversation.getContent(), 
                    request.getMessage(), response, fileContents);
                conversationService.updateConversation(conversationId, null, updatedContent, tokensUsed);
            }
            
            return ChatResponse.success(conversationId, response, tokensUsed);
            
        } catch (Exception e) {
            System.err.println("处理对话请求失败: " + e.getMessage());
            return ChatResponse.error(request.getConversationId(), "处理请求失败: " + e.getMessage());
        }
    }
    
    private String simulateClaudeResponse(String message) {
        // 这是一个模拟响应，实际应该通过WebSocket与本地客户端通信
        return "这是Claude的模拟响应。您的消息是: " + message.substring(0, Math.min(100, message.length())) + 
               (message.length() > 100 ? "..." : "");
    }
    
    private Integer estimateTokenUsage(String input, String output) {
        // 简单的Token估算，实际应该使用更精确的方法
        int inputTokens = input.length() / 4; // 粗略估算
        int outputTokens = output.length() / 4;
        return inputTokens + outputTokens;
    }
    
    private String generateConversationTitle(String message) {
        // 生成对话标题
        String title = message.length() > 50 ? message.substring(0, 50) + "..." : message;
        return title.replaceAll("\\s+", " ").trim();
    }
    
    private String buildConversationContent(String userMessage, String assistantResponse, List<String> fileContents) {
        try {
            Map<String, Object> conversation = new HashMap<>();
            List<Map<String, Object>> messages = new ArrayList<>();
            
            // 用户消息
            Map<String, Object> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            userMsg.put("timestamp", System.currentTimeMillis());
            if (!fileContents.isEmpty()) {
                userMsg.put("files", fileContents);
            }
            messages.add(userMsg);
            
            // 助手响应
            Map<String, Object> assistantMsg = new HashMap<>();
            assistantMsg.put("role", "assistant");
            assistantMsg.put("content", assistantResponse);
            assistantMsg.put("timestamp", System.currentTimeMillis());
            messages.add(assistantMsg);
            
            conversation.put("messages", messages);
            return objectMapper.writeValueAsString(conversation);
            
        } catch (JsonProcessingException e) {
            System.err.println("构建对话内容失败: " + e.getMessage());
            return "{\"messages\":[]}";
        }
    }
    
    private String appendToConversationContent(String existingContent, String userMessage, 
                                             String assistantResponse, List<String> fileContents) {
        try {
            Map<String, Object> conversation = objectMapper.readValue(existingContent, Map.class);
            List<Map<String, Object>> messages = (List<Map<String, Object>>) conversation.get("messages");
            
            if (messages == null) {
                messages = new ArrayList<>();
            }
            
            // 添加用户消息
            Map<String, Object> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            userMsg.put("timestamp", System.currentTimeMillis());
            if (!fileContents.isEmpty()) {
                userMsg.put("files", fileContents);
            }
            messages.add(userMsg);
            
            // 添加助手响应
            Map<String, Object> assistantMsg = new HashMap<>();
            assistantMsg.put("role", "assistant");
            assistantMsg.put("content", assistantResponse);
            assistantMsg.put("timestamp", System.currentTimeMillis());
            messages.add(assistantMsg);
            
            conversation.put("messages", messages);
            return objectMapper.writeValueAsString(conversation);
            
        } catch (Exception e) {
            System.err.println("更新对话内容失败: " + e.getMessage());
            return buildConversationContent(userMessage, assistantResponse, fileContents);
        }
    }
}