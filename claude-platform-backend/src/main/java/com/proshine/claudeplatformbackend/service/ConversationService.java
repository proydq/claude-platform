package com.proshine.claudeplatformbackend.service;

import com.proshine.claudeplatformbackend.entity.Conversation;
import com.proshine.claudeplatformbackend.repository.ConversationRepository;
import com.proshine.claudeplatformbackend.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConversationService {
    
    @Autowired
    private ConversationRepository conversationRepository;
    
    @Autowired
    private TokenService tokenService;
    
    @Transactional
    public Conversation createConversation(String title, String content, Integer tokensUsed) {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setTitle(title);
        conversation.setContent(content);
        conversation.setTokensUsed(tokensUsed != null ? tokensUsed : 0);
        
        conversation = conversationRepository.save(conversation);
        
        // 记录Token使用
        if (tokensUsed != null && tokensUsed > 0) {
            tokenService.recordTokenUsage(userId, conversation.getId(), tokensUsed, "CHAT");
        }
        
        return conversation;
    }
    
    @Transactional
    public Conversation updateConversation(String conversationId, String title, 
                                         String content, Integer additionalTokens) {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
            .orElseThrow(() -> new RuntimeException("对话不存在或无权限访问"));
        
        if (title != null) {
            conversation.setTitle(title);
        }
        if (content != null) {
            conversation.setContent(content);
        }
        if (additionalTokens != null && additionalTokens > 0) {
            conversation.setTokensUsed(conversation.getTokensUsed() + additionalTokens);
            // 记录Token使用
            tokenService.recordTokenUsage(userId, conversationId, additionalTokens, "CHAT");
        }
        
        return conversationRepository.save(conversation);
    }
    
    public Page<Conversation> getUserConversations(Pageable pageable) {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        return conversationRepository.findByUserIdOrderByCreatedTimeDesc(userId, pageable);
    }
    
    public Page<Conversation> searchUserConversations(String keyword, Pageable pageable) {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        return conversationRepository.findByUserIdAndKeyword(userId, keyword, pageable);
    }
    
    public Conversation getConversationById(String conversationId) {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        return conversationRepository.findByIdAndUserId(conversationId, userId)
            .orElseThrow(() -> new RuntimeException("对话不存在或无权限访问"));
    }
    
    @Transactional
    public void deleteConversation(String conversationId) {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        // 检查对话是否存在且属于当前用户
        if (!conversationRepository.findByIdAndUserId(conversationId, userId).isPresent()) {
            throw new RuntimeException("对话不存在或无权限访问");
        }
        
        conversationRepository.deleteByUserIdAndId(userId, conversationId);
    }
    
    public long getUserConversationCount(String userId) {
        return conversationRepository.countByUserId(userId);
    }
    
    public Long getUserTotalTokensUsed(String userId) {
        return conversationRepository.sumTokensUsedByUserId(userId);
    }
    
    public List<Conversation> getUserConversationsByTimeRange(String userId, Long startTime, Long endTime) {
        return conversationRepository.findByUserIdAndTimeRange(userId, startTime, endTime);
    }
}