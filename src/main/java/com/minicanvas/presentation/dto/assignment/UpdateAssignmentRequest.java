package com.minicanvas.presentation.dto.assignment;

import java.time.LocalDateTime;

public class UpdateAssignmentRequest {
    public String title;
    public String description;
    public LocalDateTime dueAt;
}