package com.proshine.claudeplatformbackend.repository;

import com.proshine.claudeplatformbackend.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, String> {
    
    Optional<SystemConfig> findByConfigKey(String configKey);
    
    boolean existsByConfigKey(String configKey);
    
    @Query("SELECT s.configValue FROM SystemConfig s WHERE s.configKey = :configKey")
    Optional<String> findConfigValueByKey(@Param("configKey") String configKey);
    
    void deleteByConfigKey(String configKey);
}