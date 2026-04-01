package com.minicanvas.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minicanvas.dal.entities.RoleEntity;
import com.minicanvas.dal.entities.UserEntity;
import com.minicanvas.dal.repositories.RoleRepository;
import com.minicanvas.dal.repositories.UserRepository;
import com.minicanvas.presentation.dto.CreateUserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerIntegrationTest {

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

        RoleEntity adminRole = new RoleEntity();
        adminRole.setName("ADMIN");

        RoleEntity teacherRole = new RoleEntity();
        teacherRole.setName("TEACHER");

        RoleEntity studentRole = new RoleEntity();
        studentRole.setName("STUDENT");

        roleRepository.save(adminRole);
        roleRepository.save(teacherRole);
        roleRepository.save(studentRole);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_asAdmin_shouldSaveTeacherInDatabase() throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.email = "teacher@test.com";
        req.password = "Test1234!";
        req.fullName = "Teacher One";
        req.role = "TEACHER";

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        UserEntity savedUser = userRepository.findByEmail("teacher@test.com").orElse(null);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getFullName()).isEqualTo("Teacher One");
        assertThat(passwordEncoder.matches("Test1234!", savedUser.getPasswordHash())).isTrue();
        assertThat(savedUser.getRoles())
                .extracting(RoleEntity::getName)
                .contains("TEACHER");
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void createUser_asStudent_shouldReturnForbidden() throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.email = "student@test.com";
        req.password = "Test1234!";
        req.fullName = "Student One";
        req.role = "STUDENT";

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}