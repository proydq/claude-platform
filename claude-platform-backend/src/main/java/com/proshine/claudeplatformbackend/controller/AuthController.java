package com.proshine.claudeplatformbackend.controller;

import com.proshine.claudeplatformbackend.dto.request.LoginRequest;
import com.proshine.claudeplatformbackend.dto.response.ApiResponse;
import com.proshine.claudeplatformbackend.dto.response.LoginResponse;
import com.proshine.claudeplatformbackend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ApiResponse.success("登录成功", response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        try {
            authService.logout();
            return ApiResponse.success("登出成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}