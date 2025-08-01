package com.proshine.claudeplatformbackend.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileUtil {
    
    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
    
    /**
     * 生成唯一文件名
     */
    public static String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uniqueName = UUID.randomUUID().toString();
        return extension.isEmpty() ? uniqueName : uniqueName + "." + extension;
    }
    
    /**
     * 检查文件类型是否允许
     */
    public static boolean isAllowedFileType(String filename) {
        String extension = getFileExtension(filename);
        
        // 允许的文件类型
        String[] allowedTypes = {
            "txt", "log", "json", "xml", "csv", "md", "yaml", "yml",
            "java", "js", "py", "go", "html", "css", "sql", "sh",
            "properties", "conf", "config", "ini"
        };
        
        for (String allowedType : allowedTypes) {
            if (allowedType.equals(extension)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 格式化文件大小
     */
    public static String formatFileSize(long sizeInBytes) {
        if (sizeInBytes <= 0) {
            return "0 B";
        }
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = sizeInBytes;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", size, units[unitIndex]);
    }
    
    /**
     * 创建目录
     */
    public static boolean createDirectories(String path) {
        try {
            Path dirPath = Paths.get(path);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            return true;
        } catch (IOException e) {
            System.err.println("创建目录失败: " + path + ", " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 删除文件
     */
    public static boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("删除文件失败: " + filePath + ", " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查文件是否存在
     */
    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }
    
    /**
     * 获取文件大小
     */
    public static long getFileSize(String filePath) {
        try {
            return Files.size(Paths.get(filePath));
        } catch (IOException e) {
            return 0;
        }
    }
    
    /**
     * 清理临时文件
     */
    public static int cleanupTempFiles(String tempDir, long maxAgeMillis) {
        int deletedCount = 0;
        File dir = new File(tempDir);
        
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                long currentTime = System.currentTimeMillis();
                for (File file : files) {
                    if (file.isFile() && (currentTime - file.lastModified()) > maxAgeMillis) {
                        if (file.delete()) {
                            deletedCount++;
                        }
                    }
                }
            }
        }
        
        return deletedCount;
    }
}