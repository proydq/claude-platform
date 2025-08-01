package com.proshine.claudeplatformbackend.service;

import com.proshine.claudeplatformbackend.dto.request.ChangePasswordRequest;
import com.proshine.claudeplatformbackend.dto.request.CreateUserRequest;
import com.proshine.claudeplatformbackend.dto.request.UpdateUserRequest;
import com.proshine.claudeplatformbackend.dto.response.UserInfo;
import com.proshine.claudeplatformbackend.entity.User;
import com.proshine.claudeplatformbackend.repository.UserRepository;
import com.proshine.claudeplatformbackend.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private TokenService tokenService;
    
    @Transactional
    public UserInfo createUser(CreateUserRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setUserPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setUserRole(request.getUserRole());
        user.setTokenLimit(request.getTokenLimit());
        user.setUserStatus("ACTIVE");
        
        user = userRepository.save(user);
        
        return buildUserInfo(user);
    }
    
    public Page<UserInfo> getUsers(String keyword, Pageable pageable) {
        Page<User> users = userRepository.findByKeyword(keyword, pageable);
        return users.map(this::buildUserInfo);
    }
    
    public UserInfo getUserById(String id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        return buildUserInfo(user);
    }
    
    public UserInfo getCurrentUserProfile() {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        return getUserById(userId);
    }
    
    @Transactional
    public UserInfo updateUser(String id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查邮箱是否已被其他用户使用
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("邮箱已存在");
            }
        }
        
        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getTokenLimit() != null) {
            user.setTokenLimit(request.getTokenLimit());
        }
        if (request.getUserStatus() != null) {
            user.setUserStatus(request.getUserStatus());
        }
        
        user = userRepository.save(user);
        return buildUserInfo(user);
    }
    
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 验证当前密码
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getUserPassword())) {
            throw new RuntimeException("当前密码不正确");
        }
        
        // 设置新密码
        user.setUserPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    
    @Transactional
    public void resetPassword(String id, String newPassword) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setUserPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    @Transactional
    public void updateUserStatus(String id, String status) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setUserStatus(status);
        userRepository.save(user);
    }
    
    @Transactional
    public void updateTokenLimit(String id, Integer tokenLimit) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setTokenLimit(tokenLimit);
        userRepository.save(user);
    }
    
    @Transactional
    public void addExtraToken(String id, Integer extraTokens) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setTokenLimit(user.getTokenLimit() + extraTokens);
        userRepository.save(user);
    }
    
    @Transactional
    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("用户不存在");
        }
        userRepository.deleteById(id);
    }
    
    public boolean checkUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public long getActiveUserCount() {
        return userRepository.countActiveUsers();
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