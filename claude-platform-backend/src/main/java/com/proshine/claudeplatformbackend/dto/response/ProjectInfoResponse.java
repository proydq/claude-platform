package com.proshine.claudeplatformbackend.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectInfoResponse {
    
    private String projectName;
    private String projectPath;
    private String absolutePath;
    
    // 项目基本信息
    private Boolean isGitRepo;
    private String gitBranch;
    private String gitRemoteUrl;
    
    // 包管理器和框架信息
    private String packageManager;
    private String framework;
    private String language;
    private List<String> languages;
    
    // 文件统计
    private Long totalFiles;
    private Long totalLines;
    private Long totalSize; // 字节
    
    // 项目结构
    private List<FileInfo> fileStructure;
    
    // 配置文件信息
    private Map<String, Object> configFiles;
    
    // 依赖信息
    private List<String> dependencies;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileInfo {
        private String name;
        private String path;
        private String type; // file, directory
        private Long size;
        private String extension;
        private Long lastModified;
        private List<FileInfo> children;
    }
}