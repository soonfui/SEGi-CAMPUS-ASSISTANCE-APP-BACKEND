package com.segi.campusassistance.controller;

import com.segi.campusassistance.dto.UserProfileResponse;
import com.segi.campusassistance.dto.UserProfileUpdateRequest;
import com.segi.campusassistance.security.UserPrincipal;
import com.segi.campusassistance.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserProfileService userProfileService;

    public UserController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, UserProfileResponse>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal) {
        UserProfileResponse response = userProfileService.getCurrentUser(requirePrincipal(principal));
        return ResponseEntity.ok(Map.of("data", response));
    }

    @PatchMapping("/me")
    public ResponseEntity<Map<String, UserProfileResponse>> updateCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        UserProfileResponse response = userProfileService.updateCurrentUser(requirePrincipal(principal), request);
        return ResponseEntity.ok(Map.of("data", response));
    }

    @PostMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadPhoto(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file) throws IOException {
        Map<String, Object> body = userProfileService.uploadPhoto(requirePrincipal(principal), file);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        userProfileService.deleteCurrentUser(requirePrincipal(principal));
        return ResponseEntity.noContent().build();
    }

    private UserPrincipal requirePrincipal(UserPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return principal;
    }
}























