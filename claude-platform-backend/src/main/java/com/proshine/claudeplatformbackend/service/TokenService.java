package com.proshine.claudeplatformbackend.service;

import com.proshine.claudeplatformbackend.entity.TokenUsage;
import com.proshine.claudeplatformbackend.entity.User;
import com.proshine.claudeplatformbackend.repository.TokenUsageRepository;
import com.proshine.claudeplatformbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class TokenService {
    
    @Autowired
    private TokenUsageRepository tokenUsageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional
    public TokenUsage recordTokenUsage(String userId, String conversationId, 
                                     Integer tokensCount, String usageType) {
        // 检查用户是否存在
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查Token额度
        Integer monthlyUsed = getMonthlyUsedTokens(userId);
        if (monthlyUsed + tokensCount > user.getTokenLimit()) {
            throw new RuntimeException("Token额度不足");
        }
        
        TokenUsage tokenUsage = new TokenUsage();
        tokenUsage.setUserId(userId);
        tokenUsage.setConversationId(conversationId);
        tokenUsage.setTokensCount(tokensCount);
        tokenUsage.setUsageType(usageType);
        tokenUsage.setUsageTime(System.currentTimeMillis());
        
        return tokenUsageRepository.save(tokenUsage);
    }
    
    public Integer getMonthlyUsedTokens(String userId) {
        // 获取本月开始和结束时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime monthEnd = monthStart.plusMonths(1).minusNanos(1);
        
        long startTime = monthStart.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endTime = monthEnd.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        Long result = tokenUsageRepository.sumTokensCountByUserIdAndTimeRange(userId, startTime, endTime);
        return result != null ? result.intValue() : 0;
    }
    
    public Integer getTotalUsedTokens(String userId) {
        Long result = tokenUsageRepository.sumTokensCountByUserId(userId);
        return result != null ? result.intValue() : 0;
    }
    
    public Page<TokenUsage> getUserTokenUsage(String userId, Pageable pageable) {
        return tokenUsageRepository.findByUserIdOrderByUsageTimeDesc(userId, pageable);
    }
    
    public List<TokenUsage> getUserTokenUsageByTimeRange(String userId, Long startTime, Long endTime) {
        return tokenUsageRepository.findByUserIdAndTimeRange(userId, startTime, endTime);
    }
    
    public List<Object[]> getDailyUsageByTimeRange(String userId, Long startTime, Long endTime) {
        return tokenUsageRepository.findDailyUsageByUserIdAndTimeRange(userId, startTime, endTime);
    }
    
    public long getUserUsageCount(String userId) {
        return tokenUsageRepository.countByUserId(userId);
    }
    
    public List<Object[]> getUsageTypeStats(String userId) {
        return tokenUsageRepository.findUsageTypeStatsByUserId(userId);
    }
    
    public boolean checkTokenAvailable(String userId, Integer requiredTokens) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Integer monthlyUsed = getMonthlyUsedTokens(userId);
        return monthlyUsed + requiredTokens <= user.getTokenLimit();
    }
    
    public Integer getRemainingTokens(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Integer monthlyUsed = getMonthlyUsedTokens(userId);
        return user.getTokenLimit() - monthlyUsed;
    }
}