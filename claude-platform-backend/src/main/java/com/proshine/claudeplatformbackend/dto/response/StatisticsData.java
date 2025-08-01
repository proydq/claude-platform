package com.proshine.claudeplatformbackend.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsData {
    
    // 基本统计
    private Long totalConversations;
    private Long totalTokensUsed;
    private Integer remainingTokens;
    private Long totalFiles;
    
    // 本月统计
    private Long monthlyConversations;
    private Long monthlyTokensUsed;
    
    // 今日统计
    private Long todayConversations;
    private Long todayTokensUsed;
    
    // 趋势数据 (日期 -> 使用量)
    private List<TrendData> trendData;
    
    // 使用类型统计
    private Map<String, Long> usageTypeStats;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData {
        private String date;
        private Long tokensUsed;
        private Long conversations;
    }
}