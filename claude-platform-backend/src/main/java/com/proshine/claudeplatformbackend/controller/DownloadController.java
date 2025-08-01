package com.proshine.claudeplatformbackend.controller;

import com.proshine.claudeplatformbackend.dto.response.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/download")
@CrossOrigin(origins = "*")
public class DownloadController {
    
    private static final String CLIENT_FILES_DIR = "./client-files";
    
    @GetMapping("/detect")
    public ApiResponse<Map<String, String>> detectSystem(HttpServletRequest request) {
        try {
            String userAgent = request.getHeader("User-Agent");
            String os = "unknown";
            String arch = "unknown";
            String recommendedClient = "unknown";
            
            if (userAgent != null) {
                String lowerUserAgent = userAgent.toLowerCase();
                
                if (lowerUserAgent.contains("windows")) {
                    os = "Windows";
                    arch = "x64";
                    recommendedClient = "windows";
                } else if (lowerUserAgent.contains("mac os x")) {
                    os = "macOS";
                    // 简单检测，实际可能需要更复杂的逻辑
                    if (lowerUserAgent.contains("intel")) {
                        arch = "intel";
                        recommendedClient = "mac-intel";
                    } else {
                        arch = "arm64";
                        recommendedClient = "mac-arm";
                    }
                } else if (lowerUserAgent.contains("linux")) {
                    os = "Linux";
                    arch = "x64";
                    recommendedClient = "linux";
                }
            }
            
            Map<String, String> result = new HashMap<>();
            result.put("os", os);
            result.put("arch", arch);
            result.put("recommendedClient", recommendedClient);
            result.put("userAgent", userAgent);
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/client/{platform}")
    public ResponseEntity<byte[]> downloadClient(@PathVariable String platform) {
        try {
            String filename = getClientFilename(platform);
            if (filename == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            Path filePath = Paths.get(CLIENT_FILES_DIR, filename);
            if (!Files.exists(filePath)) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            byte[] fileContent = Files.readAllBytes(filePath);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(fileContent.length);
            
            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/check-connection")
    public ApiResponse<Map<String, Object>> checkConnection() {
        try {
            // 这里应该检查是否有本地客户端连接到WebSocket
            // 目前先返回模拟数据
            Map<String, Object> result = new HashMap<>();
            result.put("connected", false);
            result.put("clientCount", 0);
            result.put("lastHeartbeat", null);
            result.put("message", "暂无客户端连接");
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/versions")
    public ApiResponse<Map<String, Object>> getClientVersions() {
        try {
            Map<String, Object> versions = new HashMap<>();
            versions.put("latest", "1.0.0");
            
            Map<String, Object> platforms = new HashMap<>();
            
            Map<String, Object> windows = new HashMap<>();
            windows.put("version", "1.0.0");
            windows.put("filename", "claude-connector-windows.exe");
            windows.put("size", getFileSize("claude-connector-windows.exe"));
            platforms.put("windows", windows);
            
            Map<String, Object> macIntel = new HashMap<>();
            macIntel.put("version", "1.0.0");
            macIntel.put("filename", "claude-connector-mac-intel");
            macIntel.put("size", getFileSize("claude-connector-mac-intel"));
            platforms.put("mac-intel", macIntel);
            
            Map<String, Object> macArm = new HashMap<>();
            macArm.put("version", "1.0.0");
            macArm.put("filename", "claude-connector-mac-arm");
            macArm.put("size", getFileSize("claude-connector-mac-arm"));
            platforms.put("mac-arm", macArm);
            
            Map<String, Object> linux = new HashMap<>();
            linux.put("version", "1.0.0");
            linux.put("filename", "claude-connector-linux");
            linux.put("size", getFileSize("claude-connector-linux"));
            platforms.put("linux", linux);
            
            versions.put("platforms", platforms);
            
            return ApiResponse.success(versions);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    private String getClientFilename(String platform) {
        switch (platform.toLowerCase()) {
            case "windows":
                return "claude-connector-windows.exe";
            case "mac-intel":
                return "claude-connector-mac-intel";
            case "mac-arm":
                return "claude-connector-mac-arm";
            case "linux":
                return "claude-connector-linux";
            default:
                return null;
        }
    }
    
    private long getFileSize(String filename) {
        try {
            Path filePath = Paths.get(CLIENT_FILES_DIR, filename);
            if (Files.exists(filePath)) {
                return Files.size(filePath);
            }
        } catch (IOException e) {
            // 忽略异常
        }
        return 0;
    }
}