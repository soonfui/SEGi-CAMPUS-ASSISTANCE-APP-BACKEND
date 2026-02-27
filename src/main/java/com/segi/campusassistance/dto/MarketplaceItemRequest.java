package com.segi.campusassistance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class MarketplaceItemRequest {

    @jakarta.validation.constraints.NotBlank(message = "Item name cannot be empty")
    @Size(max = 100, message = "Item name cannot exceed 100 characters")
    @JsonProperty("itemName")
    private String itemName;

    @Size(max = 50, message = "Category cannot exceed 50 characters")
    @JsonProperty("category")
    private String category;

    @Min(value = 0, message = "Price cannot be negative")
    @JsonProperty("price")
    private BigDecimal price;

    @Pattern(regexp = "New|Like New|Good|Fair|Poor", message = "Condition must be one of: New, Like New, Good, Fair, Poor")
    @JsonProperty("condition")
    private String condition;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    @JsonProperty("description")
    private String description;

    @Size(max = 200, message = "Location cannot exceed 200 characters")
    @JsonProperty("location")
    private String location;

    @Size(max = 255, message = "Image URL cannot exceed 255 characters")
    @JsonProperty("imageUrl")
    private String imageUrl;

    @Email(message = "Contact email format is invalid")
    @Size(max = 100, message = "Contact email cannot exceed 100 characters")
    @JsonProperty("contactEmail")
    private String contactEmail;

    @Pattern(regexp = "For Sale|Sold", message = "Status must be either For Sale or Sold")
    @JsonProperty("status")
    private String status;

    @JsonProperty("isActive")
    private Boolean isActive;

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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}

