package com.proshine.claudeplatformbackend.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    
    private String id;
    private String username;
    private String realName;
    private String email;
    private String userRole;
    private Integer tokenLimit;
    private String userStatus;
    private Long createdTime;
    private Long lastLoginTime;
    
    // 统计信息
    private Integer remainingTokens;
    private Integer usedTokens;
}