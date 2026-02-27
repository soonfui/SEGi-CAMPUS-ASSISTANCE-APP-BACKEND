package com.segi.campusassistance.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleLoginRequest {

    @NotBlank(message = "idToken is required")
    private String idToken;
}

