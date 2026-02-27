package com.segi.campusassistance.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.segi.campusassistance.dto.AuthResponse;
import com.segi.campusassistance.entity.User;
import com.segi.campusassistance.repository.UserRepository;
import com.segi.campusassistance.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String ADMIN_EMAIL_PATTERN = ".scaa@gmail.com";

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${google.client.id}")
    private String googleClientId;

    public AuthResponse authenticateWithGoogle(String idTokenString) {
        if (!StringUtils.hasText(idTokenString)) {
            throw new IllegalArgumentException("idToken is required");
        }

        GoogleIdToken idToken = verifyIdToken(idTokenString);
        if (idToken == null) {
            throw new IllegalArgumentException("Invalid Google ID token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Google account email is missing");
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(User::new);

        if (user.getId() == null) {
            user.setEmail(email);
        }

        // 如果是管理员邮箱（包含 .scaa@gmail.com），设置为 ADMIN
        if (email != null && email.contains(ADMIN_EMAIL_PATTERN)) {
            user.setRole(User.Role.ADMIN);
        } else if (user.getRole() == null) {
            user.setRole(User.Role.USER);
        }

        if (StringUtils.hasText(name) && !StringUtils.hasText(user.getName())) {
            user.setName(name);
        }
        if (StringUtils.hasText(picture) && !StringUtils.hasText(user.getPicture())) {
            user.setPicture(picture);
        }

        user.setLastLogin(LocalDateTime.now());

        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setPicture(user.getPicture());
        response.setRole(user.getRole().name());

        return response;
    }

    private GoogleIdToken verifyIdToken(String idTokenString) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
        try {
            return verifier.verify(idTokenString);
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalStateException("Failed to verify Google ID token", e);
        }
    }
}

