package com.proshine.claudeplatformbackend.controller;

import com.proshine.claudeplatformbackend.dto.response.ApiResponse;
import com.proshine.claudeplatformbackend.entity.FileRecord;
import com.proshine.claudeplatformbackend.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {
    
    @Autowired
    private FileService fileService;
    
    @PostMapping("/upload")
    public ApiResponse<FileRecord> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            FileRecord fileRecord = fileService.uploadFile(file);
            return ApiResponse.success("文件上传成功", fileRecord);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping
    public ApiResponse<Page<FileRecord>> getFiles(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<FileRecord> files;
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                files = fileService.searchUserFiles(keyword.trim(), pageable);
            } else {
                files = fileService.getUserFiles(pageable);
            }
            
            return ApiResponse.success(files);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ApiResponse<FileRecord> getFileById(@PathVariable String id) {
        try {
            FileRecord fileRecord = fileService.getFileById(id);
            return ApiResponse.success(fileRecord);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String id) {
        try {
            FileRecord fileRecord = fileService.getFileById(id);
            byte[] fileContent = fileService.downloadFile(id);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                URLEncoder.encode(fileRecord.getFileName(), "UTF-8"));
            headers.setContentLength(fileContent.length);
            
            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/{id}/content")
    public ApiResponse<String> getFileContent(@PathVariable String id) {
        try {
            String content = fileService.getFileContent(id);
            return ApiResponse.success(content);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteFile(@PathVariable String id) {
        try {
            fileService.deleteFile(id);
            return ApiResponse.success("文件删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/cleanup")
    public ApiResponse<Integer> cleanupExpiredFiles() {
        try {
            int deletedCount = fileService.cleanupExpiredFiles();
            return ApiResponse.success("清理完成，删除了 " + deletedCount + " 个过期文件", deletedCount);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}