package com.minicanvas.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minicanvas.dal.entities.RoleEntity;
import com.minicanvas.dal.entities.UserEntity;
import com.minicanvas.dal.repositories.RoleRepository;
import com.minicanvas.dal.repositories.UserRepository;
import com.minicanvas.presentation.dto.auth.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        RoleEntity studentRole = new RoleEntity();
        studentRole.setName("STUDENT");
        roleRepository.save(studentRole);

        UserEntity user = new UserEntity();
        user.setEmail("student@test.com");
        user.setFullName("Student One");
        user.setPasswordHash(passwordEncoder.encode("Password123!"));
        user.setEnabled(true);
        user.setRoles(new HashSet<>(Set.of(studentRole)));

        userRepository.save(user);
    }

    @Test
    void login_withValidCredentials_returnsTokenEmailAndRoles() throws Exception {
        LoginRequest request = new LoginRequest();
        request.email = "student@test.com";
        request.password = "Password123!";

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").value(not("")))
                .andExpect(jsonPath("$.email").value("student@test.com"))
                .andExpect(jsonPath("$.roles[0]").value("STUDENT"));
    }

    @Test
    void login_withUppercaseEmailAndSpaces_stillWorks() throws Exception {
        LoginRequest request = new LoginRequest();
        request.email = " STUDENT@TEST.COM ";
        request.password = "Password123!";

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("student@test.com"));
    }

    @Test
    void login_withWrongPassword_returnsUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest();
        request.email = "student@test.com";
        request.password = "WrongPassword";

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void login_withUnknownEmail_returnsUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest();
        request.email = "missing@test.com";
        request.password = "Password123!";

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void login_withDisabledUser_returnsUnauthorized() throws Exception {
        UserEntity disabledUser = new UserEntity();
        disabledUser.setEmail("disabled@test.com");
        disabledUser.setFullName("Disabled User");
        disabledUser.setPasswordHash(passwordEncoder.encode("Password123!"));
        disabledUser.setEnabled(false);
        disabledUser.setRoles(new HashSet<>());

        userRepository.save(disabledUser);

        LoginRequest request = new LoginRequest();
        request.email = "disabled@test.com";
        request.password = "Password123!";

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("User disabled"));
    }
}