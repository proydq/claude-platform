package com.proshine.claudeplatformbackend.repository;

import com.proshine.claudeplatformbackend.entity.FileRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileRecord, String> {
    
    Page<FileRecord> findByUserIdOrderByCreatedTimeDesc(String userId, Pageable pageable);
    
    Optional<FileRecord> findByIdAndUserId(String id, String userId);
    
    @Query("SELECT f FROM FileRecord f WHERE f.userId = :userId AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "f.fileName LIKE %:keyword% OR " +
           "f.fileType LIKE %:keyword%) " +
           "ORDER BY f.createdTime DESC")
    Page<FileRecord> findByUserIdAndKeyword(@Param("userId") String userId, 
                                          @Param("keyword") String keyword, 
                                          Pageable pageable);
    
    @Query("SELECT f FROM FileRecord f WHERE f.expireTime < :currentTime")
    List<FileRecord> findExpiredFiles(@Param("currentTime") Long currentTime);
    
    @Modifying
    @Query("DELETE FROM FileRecord f WHERE f.expireTime < :currentTime")
    int deleteExpiredFiles(@Param("currentTime") Long currentTime);
    
    @Query("SELECT COUNT(f) FROM FileRecord f WHERE f.userId = :userId")
    long countByUserId(@Param("userId") String userId);
    
    @Query("SELECT SUM(f.fileSize) FROM FileRecord f WHERE f.userId = :userId")
    Long sumFileSizeByUserId(@Param("userId") String userId);
    
    void deleteByUserIdAndId(String userId, String id);
}