package com.proshine.claudeplatformbackend.service;

import com.proshine.claudeplatformbackend.entity.SystemConfig;
import com.proshine.claudeplatformbackend.repository.SystemConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SystemService {
    
    @Autowired
    private SystemConfigRepository systemConfigRepository;
    
    @Transactional
    public SystemConfig saveConfig(String key, String value, String description) {
        Optional<SystemConfig> existingConfig = systemConfigRepository.findByConfigKey(key);
        
        SystemConfig config;
        if (existingConfig.isPresent()) {
            config = existingConfig.get();
            config.setConfigValue(value);
            if (description != null) {
                config.setConfigDesc(description);
            }
        } else {
            config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setConfigDesc(description);
        }
        
        return systemConfigRepository.save(config);
    }
    
    public String getConfigValue(String key) {
        return systemConfigRepository.findConfigValueByKey(key).orElse(null);
    }
    
    public String getConfigValue(String key, String defaultValue) {
        return systemConfigRepository.findConfigValueByKey(key).orElse(defaultValue);
    }
    
    public Integer getConfigValueAsInt(String key, Integer defaultValue) {
        String value = getConfigValue(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public Long getConfigValueAsLong(String key, Long defaultValue) {
        String value = getConfigValue(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public Boolean getConfigValueAsBoolean(String key, Boolean defaultValue) {
        String value = getConfigValue(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
    
    public SystemConfig getConfig(String key) {
        return systemConfigRepository.findByConfigKey(key).orElse(null);
    }
    
    public List<SystemConfig> getAllConfigs() {
        return systemConfigRepository.findAll();
    }
    
    public Map<String, String> getAllConfigsAsMap() {
        return systemConfigRepository.findAll().stream()
            .collect(Collectors.toMap(
                SystemConfig::getConfigKey,
                config -> config.getConfigValue() != null ? config.getConfigValue() : ""
            ));
    }
    
    @Transactional
    public void saveConfigs(Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            saveConfig(entry.getKey(), entry.getValue(), null);
        }
    }
    
    @Transactional
    public void deleteConfig(String key) {
        systemConfigRepository.deleteByConfigKey(key);
    }
    
    public boolean configExists(String key) {
        return systemConfigRepository.existsByConfigKey(key);
    }
    
    // 系统默认配置键常量
    public static final String DEFAULT_TOKEN_LIMIT = "default_token_limit";
    public static final String MAX_FILE_SIZE = "max_file_size";
    public static final String FILE_EXPIRE_HOURS = "file_expire_hours";
    public static final String MAX_TOKENS_PER_REQUEST = "max_tokens_per_request";
    public static final String CONVERSATION_HISTORY_DAYS = "conversation_history_days";
    public static final String AUTO_CLEANUP_ENABLED = "auto_cleanup_enabled";
    
    // 获取系统默认配置的便捷方法
    public Integer getDefaultTokenLimit() {
        return getConfigValueAsInt(DEFAULT_TOKEN_LIMIT, 5000);
    }
    
    public Long getMaxFileSize() {
        return getConfigValueAsLong(MAX_FILE_SIZE, 10485760L); // 10MB
    }
    
    public Integer getFileExpireHours() {
        return getConfigValueAsInt(FILE_EXPIRE_HOURS, 24);
    }
    
    public Integer getMaxTokensPerRequest() {
        return getConfigValueAsInt(MAX_TOKENS_PER_REQUEST, 1000);
    }
    
    public Integer getConversationHistoryDays() {
        return getConfigValueAsInt(CONVERSATION_HISTORY_DAYS, 30);
    }
    
    public Boolean isAutoCleanupEnabled() {
        return getConfigValueAsBoolean(AUTO_CLEANUP_ENABLED, true);
    }
}