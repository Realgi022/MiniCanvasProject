package com.minicanvas.presentation.dto.announcement;

import java.time.LocalDateTime;

public class AnnouncementResponse {
    public Long id;
    public String title;
    public String content;
    public String createdByEmail;
    public String createdByName;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    public AnnouncementResponse(
            Long id,
            String title,
            String content,
            String createdByEmail,
            String createdByName,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdByEmail = createdByEmail;
        this.createdByName = createdByName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}