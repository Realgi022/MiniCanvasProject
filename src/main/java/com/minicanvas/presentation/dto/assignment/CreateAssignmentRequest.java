package com.minicanvas.presentation.dto.assignment;

import java.time.LocalDateTime;

public class CreateAssignmentRequest {
    public Long classId;
    public String title;
    public String description;
    public LocalDateTime dueAt;
}