package com.minicanvas.presentation.dto.auth;

import java.util.Set;

public class LoginResponse {
    public String token;
    public String email;
    public Set<String> roles;

    public LoginResponse(String token, String email, Set<String> roles) {
        this.token = token;
        this.email = email;
        this.roles = roles;
    }
}