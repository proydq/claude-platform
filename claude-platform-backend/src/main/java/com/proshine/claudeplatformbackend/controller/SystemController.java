package com.proshine.claudeplatformbackend.controller;

import com.proshine.claudeplatformbackend.dto.response.ApiResponse;
import com.proshine.claudeplatformbackend.entity.SystemConfig;
import com.proshine.claudeplatformbackend.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
@CrossOrigin(origins = "*")
public class SystemController {
    
    @Autowired
    private SystemService systemService;
    
    @GetMapping("/settings")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<List<SystemConfig>> getSystemSettings() {
        try {
            List<SystemConfig> configs = systemService.getAllConfigs();
            return ApiResponse.success(configs);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/settings/map")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Map<String, String>> getSystemSettingsAsMap() {
        try {
            Map<String, String> configs = systemService.getAllConfigsAsMap();
            return ApiResponse.success(configs);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/settings/{key}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<SystemConfig> getSystemSetting(@PathVariable String key) {
        try {
            SystemConfig config = systemService.getConfig(key);
            if (config == null) {
                return ApiResponse.error("配置项不存在");
            }
            return ApiResponse.success(config);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/settings")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Void> updateSystemSettings(@RequestBody Map<String, String> configs) {
        try {
            systemService.saveConfigs(configs);
            return ApiResponse.success("系统设置更新成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/settings/{key}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<SystemConfig> updateSystemSetting(
            @PathVariable String key,
            @RequestParam String value,
            @RequestParam(required = false) String description) {
        try {
            SystemConfig config = systemService.saveConfig(key, value, description);
            return ApiResponse.success("配置更新成功", config);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/settings/{key}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Void> deleteSystemSetting(@PathVariable String key) {
        try {
            systemService.deleteConfig(key);
            return ApiResponse.success("配置删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    // 获取系统默认配置的快捷接口
    @GetMapping("/default-settings")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<Map<String, Object>> getDefaultSettings() {
        try {
            Map<String, Object> defaultSettings = new HashMap<>();
            defaultSettings.put("defaultTokenLimit", systemService.getDefaultTokenLimit());
            defaultSettings.put("maxFileSize", systemService.getMaxFileSize());
            defaultSettings.put("fileExpireHours", systemService.getFileExpireHours());
            defaultSettings.put("maxTokensPerRequest", systemService.getMaxTokensPerRequest());
            defaultSettings.put("conversationHistoryDays", systemService.getConversationHistoryDays());
            defaultSettings.put("autoCleanupEnabled", systemService.isAutoCleanupEnabled());
            return ApiResponse.success(defaultSettings);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}