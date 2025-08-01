package com.proshine.claudeplatformbackend.controller;

import com.proshine.claudeplatformbackend.dto.response.ApiResponse;
import com.proshine.claudeplatformbackend.dto.response.StatisticsData;
import com.proshine.claudeplatformbackend.entity.TokenUsage;
import com.proshine.claudeplatformbackend.service.StatisticsService;
import com.proshine.claudeplatformbackend.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "*")
public class StatisticsController {
    
    @Autowired
    private StatisticsService statisticsService;
    
    @Autowired
    private TokenService tokenService;
    
    @GetMapping("/my/summary")
    public ApiResponse<StatisticsData> getMyStatisticsSummary() {
        try {
            StatisticsData statistics = statisticsService.getUserStatistics();
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/my/detail")
    public ApiResponse<Page<TokenUsage>> getMyStatisticsDetail(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<TokenUsage> tokenUsages = tokenService.getUserTokenUsage(
                com.proshine.claudeplatformbackend.security.SecurityUtils.getCurrentUserId(), 
                pageable
            );
            return ApiResponse.success(tokenUsages);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/my/trend")
    public ApiResponse<List<TokenUsage>> getMyUsageTrend(
            @RequestParam(defaultValue = "30") int days) {
        try {
            String userId = com.proshine.claudeplatformbackend.security.SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return ApiResponse.error("用户未登录");
            }
            
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(days);
            
            long startTimeMs = startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long endTimeMs = endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            
            List<TokenUsage> trendData = tokenService.getUserTokenUsageByTimeRange(userId, startTimeMs, endTimeMs);
            return ApiResponse.success(trendData);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/my/today")
    public ApiResponse<StatisticsData.TrendData> getTodayStatistics() {
        try {
            StatisticsData.TrendData todayData = statisticsService.getTodayStatistics();
            return ApiResponse.success(todayData);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/my/remaining-tokens")
    public ApiResponse<Integer> getRemainingTokens() {
        try {
            String userId = com.proshine.claudeplatformbackend.security.SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return ApiResponse.error("用户未登录");
            }
            
            Integer remainingTokens = tokenService.getRemainingTokens(userId);
            return ApiResponse.success(remainingTokens);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}