package com.proshine.claudeplatformbackend.service;

import com.proshine.claudeplatformbackend.dto.response.StatisticsData;
import com.proshine.claudeplatformbackend.entity.TokenUsage;
import com.proshine.claudeplatformbackend.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService {
    
    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private ConversationService conversationService;
    
    @Autowired
    private FileService fileService;
    
    public StatisticsData getUserStatistics() {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        return getUserStatistics(userId);
    }
    
    public StatisticsData getUserStatistics(String userId) {
        StatisticsData statistics = new StatisticsData();
        
        // 基本统计
        statistics.setTotalConversations(conversationService.getUserConversationCount(userId));
        statistics.setTotalTokensUsed(tokenService.getTotalUsedTokens(userId).longValue());
        statistics.setRemainingTokens(tokenService.getRemainingTokens(userId));
        statistics.setTotalFiles(fileService.getUserFileCount(userId));
        
        // 本月统计
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime monthEnd = monthStart.plusMonths(1).minusNanos(1);
        
        long monthStartTime = monthStart.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long monthEndTime = monthEnd.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        statistics.setMonthlyConversations((long) conversationService.getUserConversationsByTimeRange(
            userId, monthStartTime, monthEndTime).size());
        statistics.setMonthlyTokensUsed(tokenService.getMonthlyUsedTokens(userId).longValue());
        
        // 今日统计
        LocalDateTime todayStart = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = todayStart.plusDays(1).minusNanos(1);
        
        long todayStartTime = todayStart.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long todayEndTime = todayEnd.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        List<TokenUsage> todayUsage = tokenService.getUserTokenUsageByTimeRange(userId, todayStartTime, todayEndTime);
        statistics.setTodayConversations((long) conversationService.getUserConversationsByTimeRange(
            userId, todayStartTime, todayEndTime).size());
        statistics.setTodayTokensUsed((long) todayUsage.stream()
            .mapToInt(TokenUsage::getTokensCount).sum());
        
        // 趋势数据（最近30天）
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        long thirtyDaysAgoTime = thirtyDaysAgo.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        List<Object[]> dailyUsage = tokenService.getDailyUsageByTimeRange(userId, thirtyDaysAgoTime, System.currentTimeMillis());
        List<StatisticsData.TrendData> trendData = new ArrayList<>();
        
        for (Object[] row : dailyUsage) {
            StatisticsData.TrendData trend = new StatisticsData.TrendData();
            trend.setDate(row[0].toString());
            trend.setTokensUsed(((Number) row[1]).longValue());
            
            // 获取当天的对话数量
            LocalDateTime date = LocalDateTime.parse(row[0].toString() + "T00:00:00");
            LocalDateTime dayStart = date.withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime dayEnd = dayStart.plusDays(1).minusNanos(1);
            
            long dayStartTime = dayStart.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long dayEndTime = dayEnd.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            
            trend.setConversations((long) conversationService.getUserConversationsByTimeRange(
                userId, dayStartTime, dayEndTime).size());
            
            trendData.add(trend);
        }
        statistics.setTrendData(trendData);
        
        // 使用类型统计
        List<Object[]> usageTypeStats = tokenService.getUsageTypeStats(userId);
        Map<String, Long> usageTypeMap = new HashMap<>();
        for (Object[] row : usageTypeStats) {
            usageTypeMap.put(row[0].toString(), ((Number) row[1]).longValue());
        }
        statistics.setUsageTypeStats(usageTypeMap);
        
        return statistics;
    }
    
    public StatisticsData.TrendData getTodayStatistics() {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = todayStart.plusDays(1).minusNanos(1);
        
        long todayStartTime = todayStart.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long todayEndTime = todayEnd.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        List<TokenUsage> todayUsage = tokenService.getUserTokenUsageByTimeRange(userId, todayStartTime, todayEndTime);
        long todayTokens = todayUsage.stream().mapToInt(TokenUsage::getTokensCount).sum();
        long todayConversations = conversationService.getUserConversationsByTimeRange(
            userId, todayStartTime, todayEndTime).size();
        
        StatisticsData.TrendData todayData = new StatisticsData.TrendData();
        todayData.setDate(now.toLocalDate().toString());
        todayData.setTokensUsed(todayTokens);
        todayData.setConversations(todayConversations);
        
        return todayData;
    }
}