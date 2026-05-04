package com.minicanvas.presentation.dto;

import java.time.LocalDateTime;

public class AiChatMessageResponse {

    private Long id;
    private String role;
    private String message;
    private LocalDateTime createdAt;

    public AiChatMessageResponse(Long id, String role, String message, LocalDateTime createdAt) {
        this.id = id;
        this.role = role;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}