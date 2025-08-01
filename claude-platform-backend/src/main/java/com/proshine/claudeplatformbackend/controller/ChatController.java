package com.proshine.claudeplatformbackend.controller;

import com.proshine.claudeplatformbackend.dto.request.ChatRequest;
import com.proshine.claudeplatformbackend.dto.response.ApiResponse;
import com.proshine.claudeplatformbackend.dto.response.ChatResponse;
import com.proshine.claudeplatformbackend.entity.Conversation;
import com.proshine.claudeplatformbackend.service.ChatService;
import com.proshine.claudeplatformbackend.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private ConversationService conversationService;
    
    @PostMapping("/send")
    public ApiResponse<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        try {
            ChatResponse response = chatService.processChat(request);
            if (response.isSuccess()) {
                return ApiResponse.success("消息发送成功", response);
            } else {
                return ApiResponse.error(response.getError(), response);
            }
        } catch (Exception e) {
            ChatResponse errorResponse = ChatResponse.error(request.getConversationId(), e.getMessage());
            return ApiResponse.error(e.getMessage(), errorResponse);
        }
    }
    
    @GetMapping("/conversations")
    public ApiResponse<Page<Conversation>> getConversations(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Conversation> conversations;
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                conversations = conversationService.searchUserConversations(keyword.trim(), pageable);
            } else {
                conversations = conversationService.getUserConversations(pageable);
            }
            
            return ApiResponse.success(conversations);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/conversations")
    public ApiResponse<Conversation> createConversation(@RequestParam String title) {
        try {
            Conversation conversation = conversationService.createConversation(title, "{\"messages\":[]}", 0);
            return ApiResponse.success("对话创建成功", conversation);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/conversations/{id}")
    public ApiResponse<Conversation> getConversationById(@PathVariable String id) {
        try {
            Conversation conversation = conversationService.getConversationById(id);
            return ApiResponse.success(conversation);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/conversations/{id}")
    public ApiResponse<Conversation> updateConversation(
            @PathVariable String id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String content) {
        try {
            Conversation conversation = conversationService.updateConversation(id, title, content, null);
            return ApiResponse.success("对话更新成功", conversation);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/conversations/{id}")
    public ApiResponse<Void> deleteConversation(@PathVariable String id) {
        try {
            conversationService.deleteConversation(id);
            return ApiResponse.success("对话删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}