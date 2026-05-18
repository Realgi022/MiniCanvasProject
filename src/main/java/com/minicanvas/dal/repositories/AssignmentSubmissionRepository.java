package com.minicanvas.dal.repositories;

import com.minicanvas.dal.entities.AssignmentSubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmissionEntity, Long> {

    Optional<AssignmentSubmissionEntity> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    List<AssignmentSubmissionEntity> findByAssignmentIdOrderBySubmittedAtDesc(Long assignmentId);

    List<AssignmentSubmissionEntity> findByAssignmentIdAndStudentFullNameContainingIgnoreCaseOrderBySubmittedAtDesc(
            Long assignmentId,
            String studentName
    );

    boolean existsByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    List<AssignmentSubmissionEntity> findByStudentEmailOrderBySubmittedAtDesc(String email);
}