package com.minicanvas.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minicanvas.dal.entities.AnnouncementEntity;
import com.minicanvas.dal.entities.RoleEntity;
import com.minicanvas.dal.entities.UserEntity;
import com.minicanvas.dal.repositories.AnnouncementRepository;
import com.minicanvas.dal.repositories.RoleRepository;
import com.minicanvas.dal.repositories.UserRepository;
import com.minicanvas.presentation.dto.announcement.CreateAnnouncementRequest;
import com.minicanvas.presentation.dto.announcement.UpdateAnnouncementRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AnnouncementControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserEntity teacher;

    @BeforeEach
    void setUp() {
        announcementRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        RoleEntity teacherRole = new RoleEntity();
        teacherRole.setName("TEACHER");

        RoleEntity studentRole = new RoleEntity();
        studentRole.setName("STUDENT");

        roleRepository.save(teacherRole);
        roleRepository.save(studentRole);

        teacher = new UserEntity();
        teacher.setEmail("teacher@test.com");
        teacher.setFullName("Teacher One");
        teacher.setPasswordHash(passwordEncoder.encode("Password123!"));
        teacher.setEnabled(true);
        teacher.setRoles(new HashSet<>(Set.of(teacherRole)));

        userRepository.save(teacher);

        UserEntity student = new UserEntity();
        student.setEmail("student@test.com");
        student.setFullName("Student One");
        student.setPasswordHash(passwordEncoder.encode("Password123!"));
        student.setEnabled(true);
        student.setRoles(new HashSet<>(Set.of(studentRole)));

        userRepository.save(student);
    }

    @Test
    @WithMockUser(username = "teacher@test.com", roles = "TEACHER")
    void createAnnouncement_asTeacher_shouldReturnOk() throws Exception {
        CreateAnnouncementRequest request = new CreateAnnouncementRequest();
        request.title = "Exam Week";
        request.content = "Check your schedule.";

        mockMvc.perform(post("/announcements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Exam Week"))
                .andExpect(jsonPath("$.content").value("Check your schedule."))
                .andExpect(jsonPath("$.createdByEmail").value("teacher@test.com"))
                .andExpect(jsonPath("$.createdByName").value("Teacher One"));
    }

    @Test
    @WithMockUser(username = "student@test.com", roles = "STUDENT")
    void createAnnouncement_asStudent_shouldReturnForbidden() throws Exception {
        CreateAnnouncementRequest request = new CreateAnnouncementRequest();
        request.title = "Student announcement";
        request.content = "Students should not create this.";

        mockMvc.perform(post("/announcements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "teacher@test.com", roles = "TEACHER")
    void createAnnouncement_withEmptyTitle_shouldReturnBadRequest() throws Exception {
        CreateAnnouncementRequest request = new CreateAnnouncementRequest();
        request.title = " ";
        request.content = "Valid content";

        mockMvc.perform(post("/announcements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Title is required"));
    }

    @Test
    @WithMockUser(username = "teacher@test.com", roles = "TEACHER")
    void getAllAnnouncements_asTeacher_shouldReturnAnnouncements() throws Exception {
        AnnouncementEntity announcement = new AnnouncementEntity();
        announcement.setTitle("Welcome");
        announcement.setContent("Welcome to MiniCanvas.");
        announcement.setCreatedBy(teacher);

        announcementRepository.save(announcement);

        mockMvc.perform(get("/announcements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Welcome"))
                .andExpect(jsonPath("$[0].content").value("Welcome to MiniCanvas."))
                .andExpect(jsonPath("$[0].createdByEmail").value("teacher@test.com"));
    }

    @Test
    @WithMockUser(username = "student@test.com", roles = "STUDENT")
    void getAllAnnouncements_asStudent_shouldReturnAnnouncements() throws Exception {
        AnnouncementEntity announcement = new AnnouncementEntity();
        announcement.setTitle("For everyone");
        announcement.setContent("Students can read announcements.");
        announcement.setCreatedBy(teacher);

        announcementRepository.save(announcement);

        mockMvc.perform(get("/announcements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("For everyone"));
    }

    @Test
    @WithMockUser(username = "teacher@test.com", roles = "TEACHER")
    void updateAnnouncement_asTeacher_shouldReturnOk() throws Exception {
        AnnouncementEntity announcement = new AnnouncementEntity();
        announcement.setTitle("Old title");
        announcement.setContent("Old content");
        announcement.setCreatedBy(teacher);

        AnnouncementEntity saved = announcementRepository.save(announcement);

        UpdateAnnouncementRequest request = new UpdateAnnouncementRequest();
        request.title = "Updated title";
        request.content = "Updated content";

        mockMvc.perform(put("/announcements/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.content").value("Updated content"));
    }

    @Test
    @WithMockUser(username = "student@test.com", roles = "STUDENT")
    void updateAnnouncement_asStudent_shouldReturnForbidden() throws Exception {
        AnnouncementEntity announcement = new AnnouncementEntity();
        announcement.setTitle("Old title");
        announcement.setContent("Old content");
        announcement.setCreatedBy(teacher);

        AnnouncementEntity saved = announcementRepository.save(announcement);

        UpdateAnnouncementRequest request = new UpdateAnnouncementRequest();
        request.title = "Student update";
        request.content = "Should not work.";

        mockMvc.perform(put("/announcements/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "teacher@test.com", roles = "TEACHER")
    void deleteAnnouncement_asTeacher_shouldReturnNoContent() throws Exception {
        AnnouncementEntity announcement = new AnnouncementEntity();
        announcement.setTitle("Delete me");
        announcement.setContent("This will be deleted.");
        announcement.setCreatedBy(teacher);

        AnnouncementEntity saved = announcementRepository.save(announcement);

        mockMvc.perform(delete("/announcements/" + saved.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "student@test.com", roles = "STUDENT")
    void deleteAnnouncement_asStudent_shouldReturnForbidden() throws Exception {
        AnnouncementEntity announcement = new AnnouncementEntity();
        announcement.setTitle("Do not delete");
        announcement.setContent("Student cannot delete this.");
        announcement.setCreatedBy(teacher);

        AnnouncementEntity saved = announcementRepository.save(announcement);

        mockMvc.perform(delete("/announcements/" + saved.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "teacher@test.com", roles = "TEACHER")
    void deleteAnnouncement_whenAnnouncementDoesNotExist_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/announcements/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Announcement not found"));
    }
}