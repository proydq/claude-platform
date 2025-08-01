package com.proshine.claudeplatformbackend.repository;

import com.proshine.claudeplatformbackend.entity.TokenUsage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenUsageRepository extends JpaRepository<TokenUsage, String> {
    
    Page<TokenUsage> findByUserIdOrderByUsageTimeDesc(String userId, Pageable pageable);
    
    @Query("SELECT SUM(t.tokensCount) FROM TokenUsage t WHERE t.userId = :userId")
    Long sumTokensCountByUserId(@Param("userId") String userId);
    
    @Query("SELECT SUM(t.tokensCount) FROM TokenUsage t WHERE t.userId = :userId AND " +
           "t.usageTime >= :startTime AND t.usageTime <= :endTime")
    Long sumTokensCountByUserIdAndTimeRange(@Param("userId") String userId,
                                          @Param("startTime") Long startTime,
                                          @Param("endTime") Long endTime);
    
    @Query("SELECT t FROM TokenUsage t WHERE t.userId = :userId AND " +
           "t.usageTime >= :startTime AND t.usageTime <= :endTime " +
           "ORDER BY t.usageTime DESC")
    List<TokenUsage> findByUserIdAndTimeRange(@Param("userId") String userId,
                                            @Param("startTime") Long startTime,
                                            @Param("endTime") Long endTime);
    
    @Query("SELECT DATE(FROM_UNIXTIME(t.usageTime/1000)) as date, SUM(t.tokensCount) as totalTokens " +
           "FROM TokenUsage t WHERE t.userId = :userId AND " +
           "t.usageTime >= :startTime AND t.usageTime <= :endTime " +
           "GROUP BY DATE(FROM_UNIXTIME(t.usageTime/1000)) " +
           "ORDER BY date DESC")
    List<Object[]> findDailyUsageByUserIdAndTimeRange(@Param("userId") String userId,
                                                     @Param("startTime") Long startTime,
                                                     @Param("endTime") Long endTime);
    
    @Query("SELECT COUNT(t) FROM TokenUsage t WHERE t.userId = :userId")
    long countByUserId(@Param("userId") String userId);
    
    @Query("SELECT t.usageType, SUM(t.tokensCount) FROM TokenUsage t WHERE t.userId = :userId " +
           "GROUP BY t.usageType")
    List<Object[]> findUsageTypeStatsByUserId(@Param("userId") String userId);
}