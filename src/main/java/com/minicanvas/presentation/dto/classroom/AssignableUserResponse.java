package com.minicanvas.presentation.dto.classroom;

public class AssignableUserResponse {
    public Long id;
    public String email;
    public String fullName;
    public String role;

    public AssignableUserResponse(Long id, String email, String fullName, String role) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }
}