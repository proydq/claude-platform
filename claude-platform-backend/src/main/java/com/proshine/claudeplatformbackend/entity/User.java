package com.proshine.claudeplatformbackend.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Column(name = "username", length = 50, unique = true, nullable = false)
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Column(name = "user_password", nullable = false)
    private String userPassword;
    
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    @Column(name = "real_name", length = 50)
    private String realName;
    
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    @Column(name = "email", length = 100)
    private String email;
    
    @NotBlank(message = "用户角色不能为空")
    @Column(name = "user_role", length = 20, nullable = false)
    private String userRole;
    
    @Column(name = "token_limit")
    private Integer tokenLimit = 5000;
    
    @Column(name = "user_status", length = 20)
    private String userStatus = "ACTIVE";
    
    @Column(name = "created_time", nullable = false)
    private Long createdTime;
    
    @Column(name = "updated_time")
    private Long updatedTime;
    
    @Column(name = "last_login_time")
    private Long lastLoginTime;
    
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