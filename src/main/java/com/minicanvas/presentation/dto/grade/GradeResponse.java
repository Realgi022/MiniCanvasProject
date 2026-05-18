package com.minicanvas.presentation.dto.grade;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GradeResponse {
    public Long submissionId;
    public Long assignmentId;
    public String assignmentTitle;
    public String className;

    public Long studentId;
    public String studentEmail;
    public String studentName;

    public BigDecimal grade;
    public String feedback;

    public LocalDateTime submittedAt;
    public LocalDateTime gradedAt;

    public String gradedByEmail;
    public String gradedByName;

    public GradeResponse(
            Long submissionId,
            Long assignmentId,
            String assignmentTitle,
            String className,
            Long studentId,
            String studentEmail,
            String studentName,
            BigDecimal grade,
            String feedback,
            LocalDateTime submittedAt,
            LocalDateTime gradedAt,
            String gradedByEmail,
            String gradedByName
    ) {
        this.submissionId = submissionId;
        this.assignmentId = assignmentId;
        this.assignmentTitle = assignmentTitle;
        this.className = className;
        this.studentId = studentId;
        this.studentEmail = studentEmail;
        this.studentName = studentName;
        this.grade = grade;
        this.feedback = feedback;
        this.submittedAt = submittedAt;
        this.gradedAt = gradedAt;
        this.gradedByEmail = gradedByEmail;
        this.gradedByName = gradedByName;
    }
}