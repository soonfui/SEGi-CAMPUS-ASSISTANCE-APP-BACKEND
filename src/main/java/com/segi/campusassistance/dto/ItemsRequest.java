package com.segi.campusassistance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class ItemsRequest {

    @NotBlank(message = "Title cannot be empty")
    @Size(max = 150, message = "Title cannot exceed 150 characters")
    private String title;

    @NotBlank(message = "Description cannot be empty")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotBlank(message = "Type cannot be empty")
    @Pattern(regexp = "lost|found", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Type must be lost or found")
    private String type;

    @NotBlank(message = "Location cannot be empty")
    @Size(max = 150, message = "Location cannot exceed 150 characters")
    private String location;

    @NotNull(message = "Lost date cannot be null")
    private LocalDate dateLost;

    @NotBlank(message = "Category cannot be empty")
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category;

    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    private String image;

    @Size(max = 100, message = "Contact info cannot exceed 100 characters")
    private String contact;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    // Getters and Setters
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
}

