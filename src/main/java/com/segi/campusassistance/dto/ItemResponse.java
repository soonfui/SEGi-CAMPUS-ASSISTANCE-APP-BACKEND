package com.segi.campusassistance.dto;

import com.segi.campusassistance.entity.Item;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ItemResponse {

    private Long id;
    private String name;
    private String description;
    private String category;
    private String status;
    private String location;
    private String contactInfo;
    private LocalDate date;
    private String imageUrl;
    private Long userId;
    private String userName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean canEdit;
    private Boolean canDelete;

    public static ItemResponse fromEntity(Item item) {
        ItemResponse response = new ItemResponse();
        response.setId(item.getId());
        response.setName(item.getName());
        response.setDescription(item.getDescription());
        response.setCategory(item.getCategory());
        response.setStatus(item.getStatus());
        response.setLocation(item.getLocation());
        response.setContactInfo(item.getContactInfo());
        response.setDate(item.getDate());
        // 修复图片URL中的旧IP地址
        response.setImageUrl(fixImageUrl(item.getImageUrl()));
        response.setUserId(item.getUserId());
        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());
        return response;
    }

    public static ItemResponse fromEntity(Item item, Long currentUserId, String currentUserRole, String userName) {
        ItemResponse response = fromEntity(item);
        response.setUserName(userName);
        
        // 权限检查：如果是自己的帖子或者是ADMIN，可以编辑和删除
        boolean isOwner = currentUserId != null && item.getUserId() != null && item.getUserId().equals(currentUserId);
        boolean isAdmin = currentUserRole != null && "ADMIN".equalsIgnoreCase(currentUserRole);
        
        // 确保权限字段始终有值（不会是null）
        response.setCanEdit(isOwner || isAdmin);
        response.setCanDelete(isOwner || isAdmin);
        
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Boolean getCanEdit() {
        return canEdit;
    }

    public void setCanEdit(Boolean canEdit) {
        this.canEdit = canEdit;
    }

    public Boolean getCanDelete() {
        return canDelete;
    }

    public void setCanDelete(Boolean canDelete) {
        this.canDelete = canDelete;
    }

    /**
     * 修复图片URL中的旧IP地址，替换为当前请求的IP
     */
    private static String fixImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return imageUrl;
        }
        
        // 如果URL不包含/uploads/，直接返回
        if (!imageUrl.contains("/uploads/")) {
            return imageUrl;
        }
        
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
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
            }
        } catch (Exception e) {
            // 如果修复失败，返回原URL
        }
        
        return imageUrl;
    }
}

