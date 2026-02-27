package com.segi.campusassistance.controller;

import com.segi.campusassistance.service.FileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/upload")
public class UploadController {

    private final FileStorageService fileStorageService;

    public UploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            // 验证文件是否存在
            if (file == null || file.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "File cannot be empty");
                return ResponseEntity.badRequest().body(error);
            }

            // 验证文件类型（可选：检查是否为图片）
            String contentType = file.getContentType();
            if (contentType != null && !contentType.startsWith("image/")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "File must be an image");
                return ResponseEntity.badRequest().body(error);
            }

            // 保存文件
            String filename = fileStorageService.storeFile(file);
            
            if (filename == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Failed to save the file");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
            }
            
            // 生成公共URL（使用请求的Host动态生成）
            String imageUrl = fileStorageService.generatePublicUrl(filename, request);

            // 返回简单JSON格式
            Map<String, String> response = new HashMap<>();
            response.put("url", imageUrl);
            
            return ResponseEntity.ok(response);
                    
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "File upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "File upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

