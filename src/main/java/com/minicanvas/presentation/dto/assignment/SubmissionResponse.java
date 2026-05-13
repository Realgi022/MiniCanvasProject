package com.minicanvas.presentation.dto.assignment;

import java.time.LocalDateTime;

public class SubmissionResponse {
    public Long id;
    public Long assignmentId;
    public String assignmentTitle;
    public Long studentId;
    public String studentEmail;
    public String studentName;
    public String originalFileName;
    public String contentType;
    public Long fileSize;
    public String comment;
    public LocalDateTime submittedAt;
    public LocalDateTime updatedAt;
    public String previewUrl;
    public String downloadUrl;

    public SubmissionResponse(
            Long id,
            Long assignmentId,
            String assignmentTitle,
            Long studentId,
            String studentEmail,
            String studentName,
            String originalFileName,
            String contentType,
            Long fileSize,
            String comment,
            LocalDateTime submittedAt,
            LocalDateTime updatedAt,
            String previewUrl,
            String downloadUrl
    ) {
        this.id = id;
        this.assignmentId = assignmentId;
        this.assignmentTitle = assignmentTitle;
        this.studentId = studentId;
        this.studentEmail = studentEmail;
        this.studentName = studentName;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.comment = comment;
        this.submittedAt = submittedAt;
        this.updatedAt = updatedAt;
        this.previewUrl = previewUrl;
        this.downloadUrl = downloadUrl;
    }
}