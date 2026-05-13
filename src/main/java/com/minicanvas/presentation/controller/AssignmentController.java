package com.minicanvas.presentation.controller;

import com.minicanvas.bll.services.AssignmentService;
import com.minicanvas.dal.entities.AssignmentSubmissionEntity;
import com.minicanvas.presentation.dto.assignment.CreateAssignmentRequest;
import com.minicanvas.presentation.dto.assignment.UpdateAssignmentRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> createAssignment(@RequestBody CreateAssignmentRequest request) {
        try {
            return ResponseEntity.ok(assignmentService.createAssignment(request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new Object() {
                public final String error = ex.getMessage();
            });
        }
    }

    @PutMapping("/{assignmentId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> updateAssignment(
            @PathVariable Long assignmentId,
            @RequestBody UpdateAssignmentRequest request
    ) {
        try {
            return ResponseEntity.ok(assignmentService.updateAssignment(assignmentId, request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new Object() {
                public final String error = ex.getMessage();
            });
        }
    }

    @DeleteMapping("/{assignmentId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> deleteAssignment(@PathVariable Long assignmentId) {
        try {
            assignmentService.deleteAssignment(assignmentId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new Object() {
                public final String error = ex.getMessage();
            });
        }
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getAssignmentsForClass(@PathVariable Long classId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsForClass(classId));
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyAssignments() {
        return ResponseEntity.ok(assignmentService.getMyAssignments());
    }

    @PostMapping("/{assignmentId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> submitAssignment(
            @PathVariable Long assignmentId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "comment", required = false) String comment
    ) {
        try {
            return ResponseEntity.ok(assignmentService.submitAssignment(assignmentId, file, comment));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new Object() {
                public final String error = ex.getMessage();
            });
        }
    }

    @GetMapping("/{assignmentId}/submissions")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getSubmissionsForAssignment(
            @PathVariable Long assignmentId,
            @RequestParam(value = "studentName", required = false) String studentName
    ) {
        try {
            return ResponseEntity.ok(
                    assignmentService.getSubmissionsForAssignment(assignmentId, studentName)
            );
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new Object() {
                public final String error = ex.getMessage();
            });
        }
    }

    @GetMapping("/submissions/{submissionId}/preview")
    public ResponseEntity<?> previewSubmission(@PathVariable Long submissionId) {
        try {
            AssignmentSubmissionEntity submission = assignmentService.getSubmissionEntity(submissionId);
            Resource resource = assignmentService.getSubmissionFile(submissionId);

            String contentType = submission.getContentType() != null
                    ? submission.getContentType()
                    : MediaType.APPLICATION_OCTET_STREAM_VALUE;

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + submission.getOriginalFileName() + "\""
                    )
                    .body(resource);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new Object() {
                public final String error = ex.getMessage();
            });
        }
    }

    @GetMapping("/submissions/{submissionId}/download")
    public ResponseEntity<?> downloadSubmission(@PathVariable Long submissionId) {
        try {
            AssignmentSubmissionEntity submission = assignmentService.getSubmissionEntity(submissionId);
            Resource resource = assignmentService.getSubmissionFile(submissionId);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + submission.getOriginalFileName() + "\""
                    )
                    .body(resource);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new Object() {
                public final String error = ex.getMessage();
            });
        }
    }
}