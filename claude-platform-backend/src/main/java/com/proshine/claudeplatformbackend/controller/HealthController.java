package com.proshine.claudeplatformbackend.controller;

import com.proshine.claudeplatformbackend.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class HealthController {
    
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", System.currentTimeMillis());
        healthInfo.put("application", "Claude Platform Backend");
        healthInfo.put("version", "1.0.0");
        
        return ApiResponse.success("服务正常运行", healthInfo);
    }
    
    @GetMapping("/")
    public ApiResponse<String> root() {
        return ApiResponse.success("Claude Platform Backend API is running!");
    }
}