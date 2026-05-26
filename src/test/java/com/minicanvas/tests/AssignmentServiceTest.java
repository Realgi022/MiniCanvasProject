package com.minicanvas.tests;

import com.minicanvas.bll.services.AssignmentService;
import com.minicanvas.dal.entities.AssignmentEntity;
import com.minicanvas.dal.entities.AssignmentSubmissionEntity;
import com.minicanvas.dal.entities.ClassEntity;
import com.minicanvas.dal.entities.UserEntity;
import com.minicanvas.dal.entities.ClassMembershipEntity;
import com.minicanvas.dal.repositories.AssignmentRepository;
import com.minicanvas.dal.repositories.AssignmentSubmissionRepository;
import com.minicanvas.dal.repositories.ClassMembershipRepository;
import com.minicanvas.dal.repositories.ClassRepository;
import com.minicanvas.dal.repositories.UserRepository;
import com.minicanvas.presentation.dto.assignment.SubmissionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssignmentServiceTest {

    @Mock
    AssignmentRepository assignmentRepository;

    @Mock
    AssignmentSubmissionRepository submissionRepository;

    @Mock
    ClassRepository classRepository;

    @Mock
    ClassMembershipRepository classMembershipRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    MultipartFile file;

    private AssignmentService assignmentService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        assignmentService = new AssignmentService(
                assignmentRepository,
                submissionRepository,
                classRepository,
                classMembershipRepository,
                userRepository,
                tempDir.toString()
        );
    }

    @Test
    void submitAssignment_createsSubmissionSuccessfully() throws Exception {
        // Mock SecurityContext
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("student@test.com", null, List.of())
        );

        // Mock UserRepository to return the current user
        UserEntity student = new UserEntity();
        student.setEmail("student@test.com");
        student.setFullName("Student One");
        // Set a fake ID using reflection
        ReflectionTestUtils.setField(student, "id", 10L);

        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(student));

        // Create assignment with non-null ClassEntity and fake ID
        AssignmentEntity assignment = new AssignmentEntity();
        assignment.setTitle("Test Assignment");
        assignment.setDescription("Description");

        ClassEntity classEntity = new ClassEntity();
        ReflectionTestUtils.setField(classEntity, "id", 1L); // fake class ID
        assignment.setClassEntity(classEntity);

        when(assignmentRepository.findById(any(Long.class))).thenReturn(Optional.of(assignment));
        when(file.getOriginalFilename()).thenReturn("test.docx");
        when(submissionRepository.save(any(AssignmentSubmissionEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Mock class membership check
        when(classMembershipRepository.existsByClassEntityIdAndUserId(1L, 10L))
                .thenReturn(true);

        // Act
        SubmissionResponse response = assignmentService.submitAssignment(1L, file, "Great work!");

        // Assert
        assertNotNull(response);
        assertEquals("test.docx", response.originalFileName);
        assertEquals("Great work!", response.comment);

        verify(assignmentRepository).findById(1L);
        verify(submissionRepository).save(any(AssignmentSubmissionEntity.class));
        verify(classMembershipRepository).existsByClassEntityIdAndUserId(1L, 10L);

        // Clean up SecurityContext
        SecurityContextHolder.clearContext();
    }

    @Test
    void submitAssignment_assignmentNotFound_throws() {
        // Mock SecurityContext
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("student@test.com", null, List.of())
        );

        // Mock UserRepository
        UserEntity student = new UserEntity();
        student.setEmail("student@test.com");
        student.setFullName("Student One");
        ReflectionTestUtils.setField(student, "id", 10L);

        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(student));

        // Assignment not found
        when(assignmentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception ex = assertThrows(RuntimeException.class,
                () -> assignmentService.submitAssignment(999L, file, "Comment"));
        assertTrue(ex.getMessage().contains("not found"));

        verify(assignmentRepository).findById(999L);
        verifyNoInteractions(submissionRepository);
        verifyNoInteractions(classMembershipRepository);

        SecurityContextHolder.clearContext();
    }
}