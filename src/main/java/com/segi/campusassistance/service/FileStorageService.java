package com.segi.campusassistance.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Value("${server.ip:}")
    private String serverIp;

    @Value("${server.port:8081}")
    private String serverPort;

    public String storeFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 获取原始文件名
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        
        // 验证文件名
        if (originalFilename.contains("..")) {
            throw new IllegalArgumentException("Filename contains an invalid path sequence: " + originalFilename);
        }

        // 获取文件扩展名
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = originalFilename.substring(lastDotIndex);
        }

        // 生成唯一文件名
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        // 创建上传目录（如果不存在）
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 保存文件
        Path targetLocation = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFilename;
    }

    /**
     * 生成公共URL（自动从请求中获取Host，如果没有请求则使用配置的IP或自动检测）
     */
    public String generatePublicUrl(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        
        // 尝试从当前请求中获取Host
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return generatePublicUrl(filename, request);
            }
        } catch (Exception e) {
            // 如果获取请求失败，继续使用配置的IP
        }
        
        // 如果没有请求上下文，使用配置的IP或自动检测
        String host;
        if (serverIp != null && !serverIp.isEmpty()) {
            host = serverIp;
        } else {
            // 尝试自动获取本机IP地址
            try {
                host = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                host = "localhost";
            }
        }
        return "http://" + host + ":" + serverPort + "/uploads/" + filename;
    }

    /**
     * 生成公共URL（动态从请求中获取Host，推荐使用此方法）
     * 这样无论在哪里运行，都能自动使用正确的IP地址
     */
    public String generatePublicUrl(String filename, HttpServletRequest request) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        try {
            // 使用请求的Host动态构建URL
            String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                    .replacePath(null)
                    .build()
                    .toUriString();
            return baseUrl + "/uploads/" + filename;
        } catch (Exception e) {
            // 如果动态获取失败，回退到使用配置的IP
            return generatePublicUrl(filename);
        }
    }

    /**
     * 修复图片URL中的旧IP地址，替换为当前请求的IP
     * 用于修复数据库中存储的旧IP地址的图片URL
     */
    public String fixImageUrl(String imageUrl, HttpServletRequest request) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return imageUrl;
        }
        
        // 如果URL不包含/uploads/，直接返回
        if (!imageUrl.contains("/uploads/")) {
            return imageUrl;
        }
        
        try {
            // 获取当前请求的基础URL
            String currentBaseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                    .replacePath(null)
                    .build()
                    .toUriString();
            
            // 提取文件名
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/uploads/") + "/uploads/".length());
            
            // 如果URL包含旧IP（192.168.100.147），替换为当前IP
            if (imageUrl.contains("192.168.100.147")) {
                return currentBaseUrl + "/uploads/" + filename;
            }
            
            // 如果URL已经是相对路径或使用当前IP，直接返回
            return imageUrl;
        } catch (Exception e) {
            // 如果修复失败，返回原URL
            return imageUrl;
        }
    }

    /**
     * 修复图片URL（无请求上下文版本，尝试从RequestContextHolder获取）
     */
    public String fixImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return imageUrl;
        }
        
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return fixImageUrl(imageUrl, request);
            }
        } catch (Exception e) {
            // 如果获取请求失败，返回原URL
        }
        
        return imageUrl;
    }
}


