package com.proshine.claudeplatformbackend.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class ProjectAnalyzeRequest {
    
    @NotBlank(message = "项目路径不能为空")
    private String projectPath;
    
    @NotBlank(message = "项目名称不能为空")
    private String projectName;
    
    // 可选：指定要分析的文件类型
    private String[] includeExtensions;
    
    // 可选：排除的目录
    private String[] excludeDirectories = {
        "node_modules", ".git", "target", "build", 
        "dist", ".idea", ".vscode", "*.log"
    };
}