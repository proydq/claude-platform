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
@Table(name = "files", 
       indexes = {@Index(name = "idx_user_time", columnList = "user_id,created_time")})
public class FileRecord {
    
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    @NotBlank(message = "用户ID不能为空")
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;
    
    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名长度不能超过255个字符")
    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;
    
    @NotBlank(message = "文件路径不能为空")
    @Size(max = 500, message = "文件路径长度不能超过500个字符")
    @Column(name = "file_path", length = 500, nullable = false)
    private String filePath;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Size(max = 50, message = "文件类型长度不能超过50个字符")
    @Column(name = "file_type", length = 50)
    private String fileType;
    
    @Column(name = "created_time", nullable = false)
    private Long createdTime;
    
    @Column(name = "expire_time")
    private Long expireTime;
    
    @PrePersist
    protected void onCreate() {
        long currentTime = System.currentTimeMillis();
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
        if (createdTime == null) {
            createdTime = currentTime;
        }
        // 默认24小时后过期
        if (expireTime == null) {
            expireTime = currentTime + 24 * 60 * 60 * 1000L;
        }
    }
}