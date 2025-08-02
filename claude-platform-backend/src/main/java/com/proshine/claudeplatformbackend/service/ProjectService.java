package com.proshine.claudeplatformbackend.service;

import com.proshine.claudeplatformbackend.dto.request.ProjectAnalyzeRequest;
import com.proshine.claudeplatformbackend.dto.response.ProjectInfoResponse;
import com.proshine.claudeplatformbackend.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Service
public class ProjectService {
    
    // 支持的配置文件
    private static final Map<String, String> CONFIG_FILES;
    static {
        Map<String, String> configFiles = new HashMap<>();
        configFiles.put("package.json", "Node.js");
        configFiles.put("pom.xml", "Maven");
        configFiles.put("build.gradle", "Gradle");
        configFiles.put("requirements.txt", "Python");
        configFiles.put("Cargo.toml", "Rust");
        configFiles.put("go.mod", "Go");
        configFiles.put("composer.json", "PHP");
        CONFIG_FILES = Collections.unmodifiableMap(configFiles);
    }
    
    // 语言扩展名映射
    private static final Map<String, String> LANGUAGE_EXTENSIONS;
    static {
        Map<String, String> extensions = new HashMap<>();
        extensions.put(".js", "JavaScript");
        extensions.put(".ts", "TypeScript");
        extensions.put(".vue", "Vue");
        extensions.put(".java", "Java");
        extensions.put(".py", "Python");
        extensions.put(".rs", "Rust");
        extensions.put(".go", "Go");
        extensions.put(".php", "PHP");
        extensions.put(".cpp", "C++");
        extensions.put(".c", "C");
        LANGUAGE_EXTENSIONS = Collections.unmodifiableMap(extensions);
    }
    
    /**
     * 分析项目信息
     */
    public ProjectInfoResponse analyzeProject(ProjectAnalyzeRequest request) {
        String projectPath = request.getProjectPath();
        
        // 验证路径
        Path path = Paths.get(projectPath);
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            throw new BusinessException(400, "项目路径不存在或不是目录: " + projectPath);
        }
        
        ProjectInfoResponse.ProjectInfoResponseBuilder builder = ProjectInfoResponse.builder()
            .projectName(request.getProjectName())
            .projectPath(projectPath)
            .absolutePath(path.toAbsolutePath().toString());
        
        // 分析 Git 信息
        analyzeGitInfo(path, builder);
        
        // 分析项目类型和框架
        analyzeProjectType(path, builder);
        
        // 分析文件统计
        analyzeFileStatistics(path, builder, request.getExcludeDirectories());
        
        // 分析项目结构
        analyzeProjectStructure(path, builder, request.getExcludeDirectories());
        
        // 分析配置文件
        analyzeConfigFiles(path, builder);
        
        return builder.build();
    }
    
    /**
     * 分析 Git 信息
     */
    private void analyzeGitInfo(Path projectPath, ProjectInfoResponse.ProjectInfoResponseBuilder builder) {
        Path gitDir = projectPath.resolve(".git");
        
        if (Files.exists(gitDir)) {
            builder.isGitRepo(true);
            
            try {
                // 读取当前分支
                Path headFile = gitDir.resolve("HEAD");
                if (Files.exists(headFile)) {
                    String headContent = new String(Files.readAllBytes(headFile)).trim();
                    if (headContent.startsWith("ref: refs/heads/")) {
                        String branch = headContent.substring("ref: refs/heads/".length());
                        builder.gitBranch(branch);
                    }
                }
                
                // 读取远程仓库URL
                Path configFile = gitDir.resolve("config");
                if (Files.exists(configFile)) {
                    String configContent = new String(Files.readAllBytes(configFile));
                    // 简单解析远程URL（实际应该用更robust的方式）
                    String[] lines = configContent.split("\n");
                    for (int i = 0; i < lines.length; i++) {
                        if (lines[i].trim().contains("[remote \"origin\"]") && i + 1 < lines.length) {
                            String urlLine = lines[i + 1].trim();
                            if (urlLine.startsWith("url = ")) {
                                builder.gitRemoteUrl(urlLine.substring(6));
                                break;
                            }
                        }
                    }
                }
                
            } catch (IOException e) {
                log.warn("读取Git信息失败: {}", e.getMessage());
            }
        } else {
            builder.isGitRepo(false);
        }
    }
    
    /**
     * 分析项目类型和框架
     */
    private void analyzeProjectType(Path projectPath, ProjectInfoResponse.ProjectInfoResponseBuilder builder) {
        Set<String> languages = new HashSet<>();
        String primaryFramework = null;
        String packageManager = null;
        
        // 检查配置文件
        for (Map.Entry<String, String> entry : CONFIG_FILES.entrySet()) {
            Path configFile = projectPath.resolve(entry.getKey());
            if (Files.exists(configFile)) {
                String framework = entry.getValue();
                if (primaryFramework == null) {
                    primaryFramework = framework;
                }
                
                // 根据配置文件推断包管理器
                switch (entry.getKey()) {
                    case "package.json":
                        packageManager = determineNodePackageManager(projectPath);
                        languages.add("JavaScript");
                        break;
                    case "pom.xml":
                        packageManager = "Maven";
                        languages.add("Java");
                        break;
                    case "build.gradle":
                        packageManager = "Gradle";
                        languages.add("Java");
                        break;
                    case "requirements.txt":
                        packageManager = "pip";
                        languages.add("Python");
                        break;
                    case "Cargo.toml":
                        packageManager = "Cargo";
                        languages.add("Rust");
                        break;
                    case "go.mod":
                        packageManager = "Go Modules";
                        languages.add("Go");
                        break;
                }
                break;
            }
        }
        
        // 扫描文件扩展名
        try {
            Files.walk(projectPath, 2)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    String fileName = file.getFileName().toString();
                    int lastDot = fileName.lastIndexOf('.');
                    if (lastDot > 0) {
                        String extension = fileName.substring(lastDot);
                        String language = LANGUAGE_EXTENSIONS.get(extension);
                        if (language != null) {
                            languages.add(language);
                        }
                    }
                });
        } catch (IOException e) {
            log.warn("扫描文件扩展名失败: {}", e.getMessage());
        }
        
        builder.framework(primaryFramework)
               .packageManager(packageManager)
               .languages(new ArrayList<>(languages))
               .language(languages.isEmpty() ? null : languages.iterator().next());
    }
    
    /**
     * 确定Node.js项目的包管理器
     */
    private String determineNodePackageManager(Path projectPath) {
        if (Files.exists(projectPath.resolve("yarn.lock"))) {
            return "Yarn";
        } else if (Files.exists(projectPath.resolve("pnpm-lock.yaml"))) {
            return "pnpm";
        } else if (Files.exists(projectPath.resolve("package-lock.json"))) {
            return "npm";
        }
        return "npm";
    }
    
    /**
     * 分析文件统计
     */
    private void analyzeFileStatistics(Path projectPath, ProjectInfoResponse.ProjectInfoResponseBuilder builder, String[] excludeDirs) {
        FileStatistics stats = new FileStatistics();
        Set<String> excludeSet = new HashSet<>(Arrays.asList(excludeDirs));
        
        try {
            Files.walkFileTree(projectPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    String dirName = dir.getFileName().toString();
                    if (excludeSet.contains(dirName)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    stats.totalFiles++;
                    stats.totalSize += attrs.size();
                    
                    // 计算代码行数（简化版本）
                    String fileName = file.getFileName().toString();
                    if (isTextFile(fileName)) {
                        try {
                            long lines = Files.lines(file).count();
                            stats.totalLines += lines;
                        } catch (IOException e) {
                            // 忽略无法读取的文件
                        }
                    }
                    
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.warn("统计文件信息失败: {}", e.getMessage());
        }
        
        builder.totalFiles(stats.totalFiles)
               .totalLines(stats.totalLines)
               .totalSize(stats.totalSize);
    }
    
    /**
     * 分析项目结构（只返回第一层）
     */
    private void analyzeProjectStructure(Path projectPath, ProjectInfoResponse.ProjectInfoResponseBuilder builder, String[] excludeDirs) {
        List<ProjectInfoResponse.FileInfo> structure = new ArrayList<>();
        Set<String> excludeSet = new HashSet<>(Arrays.asList(excludeDirs));
        
        try {
            Files.list(projectPath).forEach(path -> {
                String name = path.getFileName().toString();
                if (excludeSet.contains(name)) {
                    return;
                }
                
                try {
                    BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
                    ProjectInfoResponse.FileInfo fileInfo = ProjectInfoResponse.FileInfo.builder()
                        .name(name)
                        .path(path.toString())
                        .type(Files.isDirectory(path) ? "directory" : "file")
                        .size(attrs.size())
                        .lastModified(attrs.lastModifiedTime().toMillis())
                        .build();
                    
                    if (Files.isRegularFile(path)) {
                        int lastDot = name.lastIndexOf('.');
                        if (lastDot > 0) {
                            fileInfo.setExtension(name.substring(lastDot));
                        }
                    }
                    
                    structure.add(fileInfo);
                } catch (IOException e) {
                    log.warn("读取文件信息失败: {}", e.getMessage());
                }
            });
        } catch (IOException e) {
            log.warn("读取项目结构失败: {}", e.getMessage());
        }
        
        // 排序：目录在前，然后按名称排序
        structure.sort((a, b) -> {
            if (a.getType().equals(b.getType())) {
                return a.getName().compareToIgnoreCase(b.getName());
            }
            return a.getType().equals("directory") ? -1 : 1;
        });
        
        builder.fileStructure(structure);
    }
    
    /**
     * 分析配置文件内容
     */
    private void analyzeConfigFiles(Path projectPath, ProjectInfoResponse.ProjectInfoResponseBuilder builder) {
        Map<String, Object> configFiles = new HashMap<>();
        
        for (String configFile : CONFIG_FILES.keySet()) {
            Path configPath = projectPath.resolve(configFile);
            if (Files.exists(configPath)) {
                try {
                    // 这里可以根据文件类型解析具体内容
                    // 为简化起见，只记录文件存在
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("exists", true);
                    fileInfo.put("size", Files.size(configPath));
                    fileInfo.put("lastModified", Files.getLastModifiedTime(configPath).toMillis());
                    configFiles.put(configFile, fileInfo);
                } catch (IOException e) {
                    log.warn("读取配置文件失败: {}", e.getMessage());
                }
            }
        }
        
        builder.configFiles(configFiles);
    }
    
    /**
     * 判断是否为文本文件
     */
    private boolean isTextFile(String fileName) {
        String[] textExtensions = {
            ".java", ".js", ".ts", ".vue", ".py", ".cpp", ".c", ".h",
            ".css", ".html", ".xml", ".json", ".yml", ".yaml", ".md",
            ".txt", ".properties", ".sql", ".sh", ".bat"
        };
        
        String lowerName = fileName.toLowerCase();
        for (String ext : textExtensions) {
            if (lowerName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 文件统计内部类
     */
    private static class FileStatistics {
        long totalFiles = 0;
        long totalLines = 0;
        long totalSize = 0;
    }
}