package com.proshine.claudeplatformbackend.service;

import com.proshine.claudeplatformbackend.dto.request.LoginRequest;
import com.proshine.claudeplatformbackend.dto.response.LoginResponse;
import com.proshine.claudeplatformbackend.dto.response.UserInfo;
import com.proshine.claudeplatformbackend.entity.User;
import com.proshine.claudeplatformbackend.repository.UserRepository;
import com.proshine.claudeplatformbackend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TokenService tokenService;
    
    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );
            
            String jwt = tokenProvider.generateToken(authentication);
            
            // 更新最后登录时间
            User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            user.setLastLoginTime(System.currentTimeMillis());
            userRepository.save(user);
            
            // 构建用户信息
            UserInfo userInfo = buildUserInfo(user);
            
            return new LoginResponse(
                jwt,
                tokenProvider.getExpirationTime(),
                userInfo
            );
            
        } catch (AuthenticationException e) {
            throw new RuntimeException("用户名或密码错误");
        }
    }
    
    public void logout() {
        // JWT是无状态的，这里可以记录登出日志或清理一些缓存
        // 实际的token失效由前端删除token实现
    }
    
    private UserInfo buildUserInfo(User user) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setEmail(user.getEmail());
        userInfo.setUserRole(user.getUserRole());
        userInfo.setTokenLimit(user.getTokenLimit());
        userInfo.setUserStatus(user.getUserStatus());
        userInfo.setCreatedTime(user.getCreatedTime());
        userInfo.setLastLoginTime(user.getLastLoginTime());
        
        // 获取Token使用情况
        Integer usedTokens = tokenService.getMonthlyUsedTokens(user.getId());
        userInfo.setUsedTokens(usedTokens != null ? usedTokens : 0);
        userInfo.setRemainingTokens(user.getTokenLimit() - userInfo.getUsedTokens());
        
        return userInfo;
    }
}