package com.proshine.claudeplatformbackend.repository;

import com.proshine.claudeplatformbackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    Optional<User> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "u.username LIKE %:keyword% OR " +
           "u.realName LIKE %:keyword% OR " +
           "u.email LIKE %:keyword%)")
    Page<User> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.userStatus = 'ACTIVE'")
    long countActiveUsers();
    
    @Query("SELECT u FROM User u WHERE u.userRole = :role")
    Page<User> findByUserRole(@Param("role") String role, Pageable pageable);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.userRole = :role")
    long countByUserRole(@Param("role") String role);
}