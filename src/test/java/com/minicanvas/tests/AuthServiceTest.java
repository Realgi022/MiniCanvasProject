package com.minicanvas.tests;

import com.minicanvas.bll.services.AuthService;
import com.minicanvas.dal.entities.RoleEntity;
import com.minicanvas.dal.entities.UserEntity;
import com.minicanvas.dal.repositories.UserRepository;
import com.minicanvas.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_withValidCredentials_returnsToken() {
        RoleEntity studentRole = new RoleEntity();
        studentRole.setName("STUDENT");

        UserEntity user = new UserEntity();
        user.setEmail("student@test.com");
        user.setPasswordHash("hashed-password");
        user.setEnabled(true);
        user.setRoles(new HashSet<>(Set.of(studentRole)));

        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken("student@test.com", Set.of("STUDENT"))).thenReturn("fake-jwt-token");

        String token = authService.login(" student@test.com ", "password123");

        assertEquals("fake-jwt-token", token);

        verify(userRepository).findByEmail("student@test.com");
        verify(passwordEncoder).matches("password123", "hashed-password");
        verify(jwtService).generateToken("student@test.com", Set.of("STUDENT"));
    }

    @Test
    void login_withUnknownEmail_throwsInvalidCredentials() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login("missing@test.com", "password123")
        );

        assertEquals("Invalid credentials", ex.getMessage());

        verify(userRepository).findByEmail("missing@test.com");
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void login_withWrongPassword_throwsInvalidCredentials() {
        UserEntity user = new UserEntity();
        user.setEmail("student@test.com");
        user.setPasswordHash("hashed-password");
        user.setEnabled(true);

        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login("student@test.com", "wrong-password")
        );

        assertEquals("Invalid credentials", ex.getMessage());

        verify(userRepository).findByEmail("student@test.com");
        verify(passwordEncoder).matches("wrong-password", "hashed-password");
        verifyNoInteractions(jwtService);
    }

    @Test
    void login_withDisabledUser_throwsUserDisabled() {
        UserEntity user = new UserEntity();
        user.setEmail("disabled@test.com");
        user.setPasswordHash("hashed-password");
        user.setEnabled(false);

        when(userRepository.findByEmail("disabled@test.com")).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login("disabled@test.com", "password123")
        );

        assertEquals("User disabled", ex.getMessage());

        verify(userRepository).findByEmail("disabled@test.com");
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void login_normalizesEmailToLowerCase() {
        UserEntity user = new UserEntity();
        user.setEmail("student@test.com");
        user.setPasswordHash("hashed-password");
        user.setEnabled(true);

        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken("student@test.com", Set.of())).thenReturn("token");

        String token = authService.login(" STUDENT@TEST.COM ", "password123");

        assertEquals("token", token);

        verify(userRepository).findByEmail("student@test.com");
    }
}