package com.minicanvas.bll.services;

import com.minicanvas.dal.entities.AssignmentEntity;
import com.minicanvas.dal.entities.AssignmentSubmissionEntity;
import com.minicanvas.dal.entities.UserEntity;
import com.minicanvas.dal.repositories.AssignmentRepository;
import com.minicanvas.dal.repositories.AssignmentSubmissionRepository;
import com.minicanvas.dal.repositories.ClassMembershipRepository;
import com.minicanvas.dal.repositories.ClassRepository;
import com.minicanvas.dal.repositories.UserRepository;
import com.minicanvas.presentation.dto.assignment.AssignmentResponse;
import com.minicanvas.presentation.dto.assignment.CreateAssignmentRequest;
import com.minicanvas.presentation.dto.assignment.SubmissionResponse;
import com.minicanvas.presentation.dto.assignment.UpdateAssignmentRequest;
import com.minicanvas.presentation.dto.grade.GradeResponse;
import com.minicanvas.presentation.dto.grade.GradeSubmissionRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AssignmentService {

    private static final Logger logger = LoggerFactory.getLogger(AssignmentService.class);
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final ClassRepository classRepository;
    private final ClassMembershipRepository classMembershipRepository;
    private final UserRepository userRepository;

    private final Path assignmentUploadDir;

    public AssignmentService(
            AssignmentRepository assignmentRepository,
            AssignmentSubmissionRepository submissionRepository,
            ClassRepository classRepository,
            ClassMembershipRepository classMembershipRepository,
            UserRepository userRepository,
            @Value("${app.upload.assignment-dir}") String assignmentUploadDir
    ) {
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.classRepository = classRepository;
        this.classMembershipRepository = classMembershipRepository;
        this.userRepository = userRepository;

        this.assignmentUploadDir = Path.of(assignmentUploadDir)
                .toAbsolutePath()
                .normalize();
    }

    public AssignmentResponse createAssignment(CreateAssignmentRequest request) {
        validateAssignmentRequest(request.title);

        String email = getCurrentEmail();

        UserEntity teacher = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

        var classEntity = classRepository.findById(request.classId)
                .orElseThrow(() -> new IllegalArgumentException("Class not found"));

        AssignmentEntity assignment = new AssignmentEntity();
        assignment.setClassEntity(classEntity);
        assignment.setCreatedBy(teacher);
        assignment.setTitle(request.title.trim());
        assignment.setDescription(request.description);
        assignment.setDueAt(request.dueAt);

        AssignmentEntity saved = assignmentRepository.save(assignment);

        return toAssignmentResponse(saved, false);
    }

    public AssignmentResponse updateAssignment(Long assignmentId, UpdateAssignmentRequest request) {
        validateAssignmentRequest(request.title);

        AssignmentEntity assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        assignment.setTitle(request.title.trim());
        assignment.setDescription(request.description);
        assignment.setDueAt(request.dueAt);

        AssignmentEntity saved = assignmentRepository.save(assignment);

        return toAssignmentResponse(saved, false);
    }

    public void deleteAssignment(Long assignmentId) {
        if (!assignmentRepository.existsById(assignmentId)) {
            throw new IllegalArgumentException("Assignment not found");
        }

        assignmentRepository.deleteById(assignmentId);
    }

    public List<AssignmentResponse> getAssignmentsForClass(Long classId) {
        return assignmentRepository.findByClassEntityIdOrderByCreatedAtDescIdDesc(classId)
                .stream()
                .map(assignment -> toAssignmentResponse(assignment, false))
                .toList();
    }

    public List<AssignmentResponse> getMyAssignments() {
        UserEntity currentUser = getCurrentUser();

        List<Long> classIds = classMembershipRepository
                .findByUserEmailOrderByClassEntityNameAsc(currentUser.getEmail())
                .stream()
                .map(membership -> membership.getClassEntity().getId())
                .toList();

        if (classIds.isEmpty()) {
            return List.of();
        }

        return assignmentRepository.findByClassEntityIdInOrderByCreatedAtDescIdDesc(classIds)
                .stream()
                .map(assignment -> {
                    boolean submitted = submissionRepository.existsByAssignmentIdAndStudentId(
                            assignment.getId(),
                            currentUser.getId()
                    );

                    return toAssignmentResponse(assignment, submitted);
                })
                .toList();
    }

    public SubmissionResponse submitAssignment(Long assignmentId, MultipartFile file, String comment) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        UserEntity student = getCurrentUser();

        AssignmentEntity assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        boolean belongsToClass = classMembershipRepository
                .existsByClassEntityIdAndUserId(
                        assignment.getClassEntity().getId(),
                        student.getId()
                );

        if (!belongsToClass) {
            throw new IllegalArgumentException("You are not assigned to this assignment's class");
        }

        try {
            Files.createDirectories(assignmentUploadDir);

            String originalFileName = file.getOriginalFilename() == null
                    ? "submission"
                    : file.getOriginalFilename();

            String safeOriginalFileName = originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");

            String storedFileName = UUID.randomUUID() + "_" + safeOriginalFileName;

            Path targetPath = assignmentUploadDir.resolve(storedFileName)
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(targetPath.getParent());

            file.transferTo(targetPath.toFile());

            AssignmentSubmissionEntity submission = submissionRepository
                    .findByAssignmentIdAndStudentId(assignmentId, student.getId())
                    .orElseGet(AssignmentSubmissionEntity::new);

            submission.setAssignment(assignment);
            submission.setStudent(student);
            submission.setOriginalFileName(originalFileName);
            submission.setStoredFileName(storedFileName);
            submission.setFilePath(targetPath.toString());
            submission.setContentType(file.getContentType());
            submission.setFileSize(file.getSize());
            submission.setComment(comment);

            AssignmentSubmissionEntity saved = submissionRepository.save(submission);

            return toSubmissionResponse(saved);
        } catch (Exception ex) {
            logger.error("Failed to upload assignment file", ex);
            throw new IllegalArgumentException("Failed to upload file");
        }
    }

    public List<SubmissionResponse> getSubmissionsForAssignment(Long assignmentId, String studentName) {
        if (!assignmentRepository.existsById(assignmentId)) {
            throw new IllegalArgumentException("Assignment not found");
        }

        List<AssignmentSubmissionEntity> submissions;

        if (studentName != null && !studentName.trim().isEmpty()) {
            submissions = submissionRepository
                    .findByAssignmentIdAndStudentFullNameContainingIgnoreCaseOrderBySubmittedAtDesc(
                            assignmentId,
                            studentName.trim()
                    );
        } else {
            submissions = submissionRepository.findByAssignmentIdOrderBySubmittedAtDesc(assignmentId);
        }

        return submissions.stream()
                .map(this::toSubmissionResponse)
                .toList();
    }

    public SubmissionResponse gradeSubmission(Long submissionId, GradeSubmissionRequest request) {
        if (request.grade == null) {
            throw new IllegalArgumentException("Grade is required");
        }

        if (request.grade.compareTo(BigDecimal.ZERO) < 0 ||
                request.grade.compareTo(BigDecimal.TEN) > 0) {
            throw new IllegalArgumentException("Grade must be between 0 and 10");
        }

        UserEntity teacher = getCurrentUser();

        AssignmentSubmissionEntity submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

        submission.setGrade(request.grade);
        submission.setFeedback(request.feedback);
        submission.setGradedAt(LocalDateTime.now());
        submission.setGradedBy(teacher);

        AssignmentSubmissionEntity saved = submissionRepository.save(submission);

        return toSubmissionResponse(saved);
    }

    public List<GradeResponse> getMyGrades() {
        UserEntity student = getCurrentUser();

        return submissionRepository.findByStudentEmailOrderBySubmittedAtDesc(student.getEmail())
                .stream()
                .map(this::toGradeResponse)
                .toList();
    }

    public Resource getSubmissionFile(Long submissionId) {
        AssignmentSubmissionEntity submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

        return new FileSystemResource(submission.getFilePath());
    }

    public AssignmentSubmissionEntity getSubmissionEntity(Long submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
    }

    private void validateAssignmentRequest(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
    }

    private UserEntity getCurrentUser() {
        String email = getCurrentEmail();

        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private String getCurrentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private AssignmentResponse toAssignmentResponse(AssignmentEntity assignment, Boolean submitted) {
        return new AssignmentResponse(
                assignment.getId(),
                assignment.getClassEntity().getId(),
                assignment.getClassEntity().getName(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getDueAt(),
                assignment.getCreatedBy().getEmail(),
                assignment.getCreatedBy().getFullName(),
                assignment.getCreatedAt(),
                assignment.getUpdatedAt(),
                submitted
        );
    }

    private SubmissionResponse toSubmissionResponse(AssignmentSubmissionEntity submission) {
        UserEntity gradedBy = submission.getGradedBy();

        return new SubmissionResponse(
                submission.getId(),
                submission.getAssignment().getId(),
                submission.getAssignment().getTitle(),
                submission.getStudent().getId(),
                submission.getStudent().getEmail(),
                submission.getStudent().getFullName(),
                submission.getOriginalFileName(),
                submission.getContentType(),
                submission.getFileSize(),
                submission.getComment(),
                submission.getSubmittedAt(),
                submission.getUpdatedAt(),
                "/assignments/submissions/" + submission.getId() + "/preview",
                "/assignments/submissions/" + submission.getId() + "/download",
                submission.getGrade(),
                submission.getFeedback(),
                submission.getGradedAt(),
                gradedBy != null ? gradedBy.getEmail() : null,
                gradedBy != null ? gradedBy.getFullName() : null
        );
    }

    private GradeResponse toGradeResponse(AssignmentSubmissionEntity submission) {
        UserEntity gradedBy = submission.getGradedBy();

        return new GradeResponse(
                submission.getId(),
                submission.getAssignment().getId(),
                submission.getAssignment().getTitle(),
                submission.getAssignment().getClassEntity().getName(),
                submission.getStudent().getId(),
                submission.getStudent().getEmail(),
                submission.getStudent().getFullName(),
                submission.getGrade(),
                submission.getFeedback(),
                submission.getSubmittedAt(),
                submission.getGradedAt(),
                gradedBy != null ? gradedBy.getEmail() : null,
                gradedBy != null ? gradedBy.getFullName() : null
        );
    }
}