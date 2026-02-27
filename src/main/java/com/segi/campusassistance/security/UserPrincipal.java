package com.segi.campusassistance.security;

import java.security.Principal;

public class UserPrincipal implements Principal {

    private final Long userId;
    private final String email;
    private final String role;

    public UserPrincipal(Long userId, String email, String role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String getName() {
        return email;
    }

    @Override
    public String toString() {
        return email;
    }
}

