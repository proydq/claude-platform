package com.proshine.claudeplatformbackend.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "conversations", 
       indexes = {@Index(name = "idx_user_time", columnList = "user_id,created_time")})
public class Conversation {
    
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    @NotBlank(message = "用户ID不能为空")
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;
    
    @Size(max = 200, message = "对话标题长度不能超过200个字符")
    @Column(name = "title", length = 200)
    private String title;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "tokens_used")
    private Integer tokensUsed = 0;
    
    @Column(name = "created_time", nullable = false)
    private Long createdTime;
    
    @Column(name = "updated_time")
    private Long updatedTime;
    
    @PrePersist
    protected void onCreate() {
        long currentTime = System.currentTimeMillis();
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
        if (createdTime == null) {
            createdTime = currentTime;
        }
        updatedTime = currentTime;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedTime = System.currentTimeMillis();
    }
}