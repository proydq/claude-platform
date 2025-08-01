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
@Table(name = "system_config")
public class SystemConfig {
    
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    @NotBlank(message = "配置键不能为空")
    @Size(max = 100, message = "配置键长度不能超过100个字符")
    @Column(name = "config_key", length = 100, unique = true, nullable = false)
    private String configKey;
    
    @Size(max = 500, message = "配置值长度不能超过500个字符")
    @Column(name = "config_value", length = 500)
    private String configValue;
    
    @Size(max = 200, message = "配置描述长度不能超过200个字符")
    @Column(name = "config_desc", length = 200)
    private String configDesc;
    
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