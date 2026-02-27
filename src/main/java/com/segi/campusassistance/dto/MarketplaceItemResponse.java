package com.segi.campusassistance.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.segi.campusassistance.entity.MarketplaceItem;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MarketplaceItemResponse {

    @JsonProperty("itemId")
    @JsonAlias("item_id")
    private Long itemId;

    @JsonProperty("sellerId")
    @JsonAlias("seller_id")
    private Long sellerId;

    @JsonProperty("itemName")
    @JsonAlias("item_name")
    private String itemName;

    @JsonProperty("category")
    private String category;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("condition")
    private String condition;

    @JsonProperty("description")
    private String description;

    @JsonProperty("location")
    private String location;

    @JsonProperty("imageUrl")
    @JsonAlias("image_url")
    private String imageUrl;

    @JsonProperty("contactEmail")
    @JsonAlias("contact_email")
    private String contactEmail;

    @JsonProperty("status")
    private String status;

    @JsonProperty("views")
    private Integer views;

    @JsonProperty("isActive")
    @JsonAlias("is_active")
    private Boolean isActive;

    @JsonProperty("datePosted")
    @JsonAlias("date_posted")
    private LocalDateTime datePosted;

    @JsonProperty("canEdit")
    private Boolean canEdit;

    @JsonProperty("canDelete")
    private Boolean canDelete;

    @JsonProperty("sellerName")
    @JsonAlias("seller_name")
    private String sellerName;

    @JsonProperty("sellerEmail")
    @JsonAlias("seller_email")
    private String sellerEmail;

    public static MarketplaceItemResponse fromEntity(MarketplaceItem item) {
        MarketplaceItemResponse response = new MarketplaceItemResponse();
        response.setItemId(item.getItemId());
        response.setSellerId(item.getSellerId());
        response.setItemName(item.getItemName());
        response.setCategory(item.getCategory());
        response.setPrice(item.getPrice());
        response.setCondition(item.getCondition());
        response.setDescription(item.getDescription());
        response.setLocation(item.getLocation());
        // 修复图片URL中的旧IP地址
        response.setImageUrl(fixImageUrl(item.getImageUrl()));
        response.setContactEmail(item.getContactEmail());
        response.setStatus(item.getStatus());
        response.setViews(item.getViews());
        response.setIsActive(item.getIsActive());
        response.setDatePosted(item.getDatePosted());
        return response;
    }

    public static MarketplaceItemResponse fromEntity(MarketplaceItem item, Long currentUserId, String currentUserRole, String sellerName, String sellerEmail) {
        MarketplaceItemResponse response = fromEntity(item);
        response.setSellerName(sellerName);
        response.setSellerEmail(sellerEmail);

        // 权限检查：如果是自己的商品或者是ADMIN，可以编辑和删除
        boolean isOwner = currentUserId != null && item.getSellerId() != null && item.getSellerId().equals(currentUserId);
        boolean isAdmin = currentUserRole != null && "ADMIN".equalsIgnoreCase(currentUserRole);

        // 确保权限字段始终有值（不会是null）
        response.setCanEdit(isOwner || isAdmin);
        response.setCanDelete(isOwner || isAdmin);

        return response;
    }

    // Getters and Setters
    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getDatePosted() {
        return datePosted;
    }

    public void setDatePosted(LocalDateTime datePosted) {
        this.datePosted = datePosted;
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

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getSellerEmail() {
        return sellerEmail;
    }

    public void setSellerEmail(String sellerEmail) {
        this.sellerEmail = sellerEmail;
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

