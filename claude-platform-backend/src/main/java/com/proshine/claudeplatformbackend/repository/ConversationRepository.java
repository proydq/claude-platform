package com.proshine.claudeplatformbackend.repository;

import com.proshine.claudeplatformbackend.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {
    
    Page<Conversation> findByUserIdOrderByCreatedTimeDesc(String userId, Pageable pageable);
    
    Optional<Conversation> findByIdAndUserId(String id, String userId);
    
    @Query("SELECT c FROM Conversation c WHERE c.userId = :userId AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "c.title LIKE %:keyword% OR " +
           "c.content LIKE %:keyword%) " +
           "ORDER BY c.createdTime DESC")
    Page<Conversation> findByUserIdAndKeyword(@Param("userId") String userId, 
                                            @Param("keyword") String keyword, 
                                            Pageable pageable);
    
    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.userId = :userId")
    long countByUserId(@Param("userId") String userId);
    
    @Query("SELECT SUM(c.tokensUsed) FROM Conversation c WHERE c.userId = :userId")
    Long sumTokensUsedByUserId(@Param("userId") String userId);
    
    @Query("SELECT c FROM Conversation c WHERE c.userId = :userId AND " +
           "c.createdTime >= :startTime AND c.createdTime <= :endTime " +
           "ORDER BY c.createdTime DESC")
    List<Conversation> findByUserIdAndTimeRange(@Param("userId") String userId,
                                              @Param("startTime") Long startTime,
                                              @Param("endTime") Long endTime);
    
    void deleteByUserIdAndId(String userId, String id);
}