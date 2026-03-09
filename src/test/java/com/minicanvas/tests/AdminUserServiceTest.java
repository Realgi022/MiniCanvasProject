package com.minicanvas.tests;

import com.minicanvas.bll.services.AdminUserService;
import com.minicanvas.dal.entities.RoleEntity;
import com.minicanvas.dal.entities.UserEntity;
import com.minicanvas.dal.repositories.RoleRepository;
import com.minicanvas.dal.repositories.UserRepository;
import com.minicanvas.presentation.dto.CreateUserRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    AdminUserService adminUserService;

    @Test
    void createUser_teacher_success() {
        // arrange
        CreateUserRequest req = new CreateUserRequest();
        req.email = "teacher@test.com";
        req.password = "Test1234!";
        req.fullName = "Teacher One";
        req.role = "TEACHER";

        when(userRepository.existsByEmail("teacher@test.com")).thenReturn(false);

        RoleEntity teacherRole = new RoleEntity();
        teacherRole.setName("TEACHER");
        when(roleRepository.findByName("TEACHER")).thenReturn(Optional.of(teacherRole));

        when(passwordEncoder.encode("Test1234!")).thenReturn("HASHED");

        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // act
        UserEntity created = adminUserService.createUser(req);

        // assert
        assertEquals("teacher@test.com", created.getEmail());
        assertEquals("HASHED", created.getPasswordHash());
        assertTrue(created.getRoles().stream().anyMatch(r -> "TEACHER".equals(r.getName())));

        verify(userRepository).existsByEmail("teacher@test.com");
        verify(roleRepository).findByName("TEACHER");
        verify(passwordEncoder).encode("Test1234!");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void createUser_rejects_invalid_role() {
        CreateUserRequest req = new CreateUserRequest();
        req.email = "a@test.com";
        req.password = "x";
        req.role = "HACKER";

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> adminUserService.createUser(req));

        assertTrue(ex.getMessage().toLowerCase().contains("teacher") || ex.getMessage().toLowerCase().contains("student"));
        verifyNoInteractions(userRepository, roleRepository, passwordEncoder);
    }

    @Test
    void createUser_rejects_duplicate_email() {
        CreateUserRequest req = new CreateUserRequest();
        req.email = "dup@test.com";
        req.password = "x";
        req.role = "STUDENT";

        when(userRepository.existsByEmail("dup@test.com")).thenReturn(true);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> adminUserService.createUser(req));

        assertTrue(ex.getMessage().toLowerCase().contains("exists"));
        verify(userRepository).existsByEmail("dup@test.com");
        verifyNoMoreInteractions(roleRepository, passwordEncoder);
    }
}