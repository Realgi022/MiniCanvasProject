package com.minicanvas.presentation.dto.auth;

import java.util.Set;

public class MeResponse {
    public Long id;
    public String email;
    public String fullName;
    public Set<String> roles;

    public MeResponse(Long id, String email, String fullName, Set<String> roles) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles;
    }
}