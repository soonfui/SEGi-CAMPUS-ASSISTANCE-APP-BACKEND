package com.segi.campusassistance.dto;

import com.segi.campusassistance.entity.Items;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ItemsResponse {

    private Long id;
    private String title;
    private String description;
    private String type;
    private String location;
    private LocalDate dateLost;
    private String category;
    private String image;
    private String contact;
    private Long userId;
    private String userName;
    private LocalDateTime createdAt;
    private Boolean canEdit;
    private Boolean canDelete;

    public static ItemsResponse fromEntity(Items items) {
        ItemsResponse response = new ItemsResponse();
        response.setId(items.getId());
        response.setTitle(items.getTitle());
        response.setDescription(items.getDescription());
        response.setType(items.getType());
        response.setLocation(items.getLocation());
        response.setDateLost(items.getDateLost());
        response.setCategory(items.getCategory());
        // 修复图片URL中的旧IP地址
        response.setImage(fixImageUrl(items.getImage()));
        response.setContact(items.getContact());
        response.setUserId(items.getUserId());
        response.setCreatedAt(items.getCreatedAt());
        return response;
    }

    public static ItemsResponse fromEntity(Items items, Long currentUserId, String currentUserRole, String userName) {
        ItemsResponse response = fromEntity(items);
        response.setUserName(userName);
        
        // 权限检查：如果是自己的帖子或者是ADMIN，可以编辑和删除
        boolean isOwner = currentUserId != null && items.getUserId() != null && items.getUserId().equals(currentUserId);
        boolean isAdmin = currentUserRole != null && "ADMIN".equalsIgnoreCase(currentUserRole);
        
        // 确保权限字段始终有值（不会是null）
        response.setCanEdit(isOwner || isAdmin);
        response.setCanDelete(isOwner || isAdmin);
        
        // 调试日志
        System.out.println("DEBUG: Item " + items.getId() + 
            " - itemUserId: " + items.getUserId() + 
            ", currentUserId: " + currentUserId + 
            ", currentUserRole: " + currentUserRole + 
            ", isOwner: " + isOwner + 
            ", isAdmin: " + isAdmin + 
            ", canEdit: " + response.getCanEdit() + 
            ", canDelete: " + response.getCanDelete());
        
        return response;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getDateLost() {
        return dateLost;
    }

    public void setDateLost(LocalDate dateLost) {
        this.dateLost = dateLost;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

