package com.proshine.claudeplatformbackend.controller;

import com.proshine.claudeplatformbackend.dto.request.ProjectAnalyzeRequest;
import com.proshine.claudeplatformbackend.dto.response.ApiResponse;
import com.proshine.claudeplatformbackend.dto.response.ProjectInfoResponse;
import com.proshine.claudeplatformbackend.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {
    
    private final ProjectService projectService;
    
    /**
     * 分析项目信息
     */
    @PostMapping("/analyze")
    public ApiResponse<ProjectInfoResponse> analyzeProject(@Valid @RequestBody ProjectAnalyzeRequest request) {
        log.info("分析项目: {}", request.getProjectPath());
        
        ProjectInfoResponse projectInfo = projectService.analyzeProject(request);
        
        return ApiResponse.success("项目分析完成", projectInfo);
    }
    
    /**
     * 验证项目路径是否有效
     */
    @PostMapping("/validate-path")
    public ApiResponse<Boolean> validateProjectPath(@RequestBody String projectPath) {
        log.info("验证项目路径: {}", projectPath);
        
        try {
            java.nio.file.Path path = java.nio.file.Paths.get(projectPath);
            boolean isValid = java.nio.file.Files.exists(path) && java.nio.file.Files.isDirectory(path);
            
            if (isValid) {
                return ApiResponse.success("路径有效", true);
            } else {
                return ApiResponse.error("路径无效或不是目录", false);
            }
        } catch (Exception e) {
            log.warn("路径验证失败: {}", e.getMessage());
            return ApiResponse.error("路径格式错误", false);
        }
    }
}