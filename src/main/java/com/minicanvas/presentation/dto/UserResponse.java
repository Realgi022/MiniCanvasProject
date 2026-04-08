package com.minicanvas.presentation.dto;

public class UserResponse {
    public Long id;
    public String fullName;
    public String email;
    public boolean enabled;
    public String role;

    public UserResponse() {
    }

    public UserResponse(Long id, String fullName, String email, boolean enabled, String role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.enabled = enabled;
        this.role = role;
    }
}