package com.proshine.claudeplatformbackend.controller;

import com.proshine.claudeplatformbackend.dto.request.ChangePasswordRequest;
import com.proshine.claudeplatformbackend.dto.request.CreateUserRequest;
import com.proshine.claudeplatformbackend.dto.request.UpdateUserRequest;
import com.proshine.claudeplatformbackend.dto.response.ApiResponse;
import com.proshine.claudeplatformbackend.dto.response.UserInfo;
import com.proshine.claudeplatformbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    // 管理员接口
    @PostMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<UserInfo> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            UserInfo userInfo = userService.createUser(request);
            return ApiResponse.success("用户创建成功", userInfo);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Page<UserInfo>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<UserInfo> users = userService.getUsers(keyword, pageable);
            return ApiResponse.success(users);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<UserInfo> getUserById(@PathVariable String id) {
        try {
            UserInfo userInfo = userService.getUserById(id);
            return ApiResponse.success(userInfo);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<UserInfo> updateUser(@PathVariable String id, 
                                          @Valid @RequestBody UpdateUserRequest request) {
        try {
            UserInfo userInfo = userService.updateUser(id, request);
            return ApiResponse.success("用户信息更新成功", userInfo);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/users/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Void> updateUserStatus(@PathVariable String id, 
                                            @RequestParam String status) {
        try {
            userService.updateUserStatus(id, status);
            return ApiResponse.success("用户状态更新成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/users/{id}/password")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Void> resetPassword(@PathVariable String id, 
                                         @RequestParam String newPassword) {
        try {
            userService.resetPassword(id, newPassword);
            return ApiResponse.success("密码重置成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/users/{id}/token-limit")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Void> updateTokenLimit(@PathVariable String id, 
                                            @RequestParam Integer tokenLimit) {
        try {
            userService.updateTokenLimit(id, tokenLimit);
            return ApiResponse.success("Token额度更新成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/users/{id}/extra-token")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Void> addExtraToken(@PathVariable String id, 
                                         @RequestParam Integer extraTokens) {
        try {
            userService.addExtraToken(id, extraTokens);
            return ApiResponse.success("临时额度添加成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Void> deleteUser(@PathVariable String id) {
        try {
            userService.deleteUser(id);
            return ApiResponse.success("用户删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/users/check-username")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Boolean> checkUsernameExists(@RequestParam String username) {
        try {
            boolean exists = userService.checkUsernameExists(username);
            return ApiResponse.success(exists);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    // 个人中心接口
    @GetMapping("/profile")
    public ApiResponse<UserInfo> getCurrentUserProfile() {
        try {
            UserInfo userInfo = userService.getCurrentUserProfile();
            return ApiResponse.success(userInfo);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/profile/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(request);
            return ApiResponse.success("密码修改成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}