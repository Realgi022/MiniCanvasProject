package com.minicanvas.presentation.dto.assignment;

import java.time.LocalDateTime;

public class AssignmentResponse {
    public Long id;
    public Long classId;
    public String className;
    public String title;
    public String description;
    public LocalDateTime dueAt;
    public String createdByEmail;
    public String createdByName;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public Boolean submitted;

    public AssignmentResponse(
            Long id,
            Long classId,
            String className,
            String title,
            String description,
            LocalDateTime dueAt,
            String createdByEmail,
            String createdByName,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Boolean submitted
    ) {
        this.id = id;
        this.classId = classId;
        this.className = className;
        this.title = title;
        this.description = description;
        this.dueAt = dueAt;
        this.createdByEmail = createdByEmail;
        this.createdByName = createdByName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.submitted = submitted;
    }
}