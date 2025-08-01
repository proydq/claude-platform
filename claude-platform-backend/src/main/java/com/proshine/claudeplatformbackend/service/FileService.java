package com.proshine.claudeplatformbackend.service;

import com.proshine.claudeplatformbackend.entity.FileRecord;
import com.proshine.claudeplatformbackend.repository.FileRepository;
import com.proshine.claudeplatformbackend.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {
    
    @Autowired
    private FileRepository fileRepository;
    
    @Value("${app.file.upload-dir:./uploads}")
    private String uploadDir;
    
    @Transactional
    public FileRecord uploadFile(MultipartFile file) throws IOException {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        // 创建上传目录
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        
        // 保存文件
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // 保存文件记录
        FileRecord fileRecord = new FileRecord();
        fileRecord.setUserId(userId);
        fileRecord.setFileName(originalFilename);
        fileRecord.setFilePath(filePath.toString());
        fileRecord.setFileSize(file.getSize());
        fileRecord.setFileType(file.getContentType());
        
        return fileRepository.save(fileRecord);
    }
    
    public Page<FileRecord> getUserFiles(Pageable pageable) {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        return fileRepository.findByUserIdOrderByCreatedTimeDesc(userId, pageable);
    }
    
    public Page<FileRecord> searchUserFiles(String keyword, Pageable pageable) {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        return fileRepository.findByUserIdAndKeyword(userId, keyword, pageable);
    }
    
    public FileRecord getFileById(String fileId) {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        return fileRepository.findByIdAndUserId(fileId, userId)
            .orElseThrow(() -> new RuntimeException("文件不存在或无权限访问"));
    }
    
    public byte[] downloadFile(String fileId) throws IOException {
        FileRecord fileRecord = getFileById(fileId);
        Path filePath = Paths.get(fileRecord.getFilePath());
        
        if (!Files.exists(filePath)) {
            throw new RuntimeException("文件不存在");
        }
        
        return Files.readAllBytes(filePath);
    }
    
    @Transactional
    public void deleteFile(String fileId) throws IOException {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        FileRecord fileRecord = fileRepository.findByIdAndUserId(fileId, userId)
            .orElseThrow(() -> new RuntimeException("文件不存在或无权限访问"));
        
        // 删除物理文件
        Path filePath = Paths.get(fileRecord.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
        
        // 删除数据库记录
        fileRepository.deleteByUserIdAndId(userId, fileId);
    }
    
    @Transactional
    public int cleanupExpiredFiles() {
        long currentTime = System.currentTimeMillis();
        List<FileRecord> expiredFiles = fileRepository.findExpiredFiles(currentTime);
        
        // 删除物理文件
        for (FileRecord fileRecord : expiredFiles) {
            try {
                Path filePath = Paths.get(fileRecord.getFilePath());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            } catch (IOException e) {
                // 记录日志但不抛出异常
                System.err.println("删除过期文件失败: " + fileRecord.getFilePath() + ", " + e.getMessage());
            }
        }
        
        // 删除数据库记录
        return fileRepository.deleteExpiredFiles(currentTime);
    }
    
    public long getUserFileCount(String userId) {
        return fileRepository.countByUserId(userId);
    }
    
    public Long getUserTotalFileSize(String userId) {
        return fileRepository.sumFileSizeByUserId(userId);
    }
    
    public boolean fileExists(String fileId) {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return false;
        }
        
        return fileRepository.findByIdAndUserId(fileId, userId).isPresent();
    }
    
    public String getFileContent(String fileId) throws IOException {
        FileRecord fileRecord = getFileById(fileId);
        Path filePath = Paths.get(fileRecord.getFilePath());
        
        if (!Files.exists(filePath)) {
            throw new RuntimeException("文件不存在");
        }
        
        return new String(Files.readAllBytes(filePath), "UTF-8");
    }
}