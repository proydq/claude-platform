package com.proshine.claudeplatformbackend.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "token_usage", 
       indexes = {@Index(name = "idx_user_time", columnList = "user_id,usage_time")})
public class TokenUsage {
    
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    @NotBlank(message = "用户ID不能为空")
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;
    
    @Column(name = "conversation_id", length = 36)
    private String conversationId;
    
    @NotNull(message = "Token数量不能为空")
    @Positive(message = "Token数量必须大于0")
    @Column(name = "tokens_count", nullable = false)
    private Integer tokensCount;
    
    @Size(max = 20, message = "使用类型长度不能超过20个字符")
    @Column(name = "usage_type", length = 20)
    private String usageType;
    
    @NotNull(message = "使用时间不能为空")
    @Column(name = "usage_time", nullable = false)
    private Long usageTime;
    
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
        if (usageTime == null) {
            usageTime = System.currentTimeMillis();
        }
    }
}