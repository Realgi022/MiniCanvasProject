package com.minicanvas.presentation.dto.classroom;

public class ClassMemberResponse {
    public Long userId;
    public String email;
    public String fullName;
    public String classRole;

    public ClassMemberResponse(
            Long userId,
            String email,
            String fullName,
            String classRole
    ) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.classRole = classRole;
    }
}