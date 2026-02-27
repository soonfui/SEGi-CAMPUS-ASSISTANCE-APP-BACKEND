package com.segi.campusassistance.dto;

import jakarta.validation.constraints.NotBlank;

public class UserProfileUpdateRequest {

    @NotBlank(message = "fullName is required")
    private String fullName;

    private String photoUrl;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}


