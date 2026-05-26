package com.minicanvas.tests;

import com.minicanvas.bll.services.AnnouncementService;
import com.minicanvas.dal.entities.AnnouncementEntity;
import com.minicanvas.dal.entities.UserEntity;
import com.minicanvas.dal.repositories.AnnouncementRepository;
import com.minicanvas.dal.repositories.UserRepository;
import com.minicanvas.presentation.dto.announcement.AnnouncementResponse;
import com.minicanvas.presentation.dto.announcement.CreateAnnouncementRequest;
import com.minicanvas.presentation.dto.announcement.UpdateAnnouncementRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

    @Mock
    private AnnouncementRepository announcementRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AnnouncementService announcementService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createAnnouncement_withValidRequest_savesAnnouncement() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("teacher@test.com", null)
        );

        UserEntity teacher = new UserEntity();
        teacher.setEmail("teacher@test.com");
        teacher.setFullName("Teacher One");

        CreateAnnouncementRequest request = new CreateAnnouncementRequest();
        request.title = " Exam Week ";
        request.content = " Check your schedule. ";

        when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(teacher));

        when(announcementRepository.save(any(AnnouncementEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AnnouncementResponse response = announcementService.createAnnouncement(request);

        assertEquals("Exam Week", response.title);
        assertEquals("Check your schedule.", response.content);
        assertEquals("teacher@test.com", response.createdByEmail);
        assertEquals("Teacher One", response.createdByName);

        ArgumentCaptor<AnnouncementEntity> captor = ArgumentCaptor.forClass(AnnouncementEntity.class);
        verify(announcementRepository).save(captor.capture());

        AnnouncementEntity savedAnnouncement = captor.getValue();

        assertEquals("Exam Week", savedAnnouncement.getTitle());
        assertEquals("Check your schedule.", savedAnnouncement.getContent());
        assertEquals(teacher, savedAnnouncement.getCreatedBy());
    }

    @Test
    void createAnnouncement_whenTeacherNotFound_throwsException() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("missing@test.com", null)
        );

        CreateAnnouncementRequest request = new CreateAnnouncementRequest();
        request.title = "Title";
        request.content = "Content";

        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> announcementService.createAnnouncement(request)
        );

        assertEquals("Teacher not found", ex.getMessage());

        verify(userRepository).findByEmail("missing@test.com");
        verifyNoInteractions(announcementRepository);
    }

    @Test
    void createAnnouncement_withEmptyTitle_throwsException() {
        CreateAnnouncementRequest request = new CreateAnnouncementRequest();
        request.title = " ";
        request.content = "Valid content";

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> announcementService.createAnnouncement(request)
        );

        assertEquals("Title is required", ex.getMessage());

        verifyNoInteractions(userRepository, announcementRepository);
    }

    @Test
    void createAnnouncement_withEmptyContent_throwsException() {
        CreateAnnouncementRequest request = new CreateAnnouncementRequest();
        request.title = "Valid title";
        request.content = " ";

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> announcementService.createAnnouncement(request)
        );

        assertEquals("Content is required", ex.getMessage());

        verifyNoInteractions(userRepository, announcementRepository);
    }

    @Test
    void updateAnnouncement_withValidRequest_updatesAnnouncement() {
        UserEntity teacher = new UserEntity();
        teacher.setEmail("teacher@test.com");
        teacher.setFullName("Teacher One");

        AnnouncementEntity announcement = new AnnouncementEntity();
        announcement.setTitle("Old title");
        announcement.setContent("Old content");
        announcement.setCreatedBy(teacher);

        UpdateAnnouncementRequest request = new UpdateAnnouncementRequest();
        request.title = " New title ";
        request.content = " New content ";

        when(announcementRepository.findById(1L)).thenReturn(Optional.of(announcement));
        when(announcementRepository.save(any(AnnouncementEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AnnouncementResponse response = announcementService.updateAnnouncement(1L, request);

        assertEquals("New title", response.title);
        assertEquals("New content", response.content);
        assertEquals("teacher@test.com", response.createdByEmail);

        verify(announcementRepository).findById(1L);
        verify(announcementRepository).save(announcement);
    }

    @Test
    void updateAnnouncement_whenAnnouncementNotFound_throwsException() {
        UpdateAnnouncementRequest request = new UpdateAnnouncementRequest();
        request.title = "Title";
        request.content = "Content";

        when(announcementRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> announcementService.updateAnnouncement(99L, request)
        );

        assertEquals("Announcement not found", ex.getMessage());

        verify(announcementRepository).findById(99L);
        verify(announcementRepository, never()).save(any());
    }

    @Test
    void deleteAnnouncement_whenAnnouncementExists_deletesAnnouncement() {
        when(announcementRepository.existsById(1L)).thenReturn(true);

        announcementService.deleteAnnouncement(1L);

        verify(announcementRepository).existsById(1L);
        verify(announcementRepository).deleteById(1L);
    }

    @Test
    void deleteAnnouncement_whenAnnouncementDoesNotExist_throwsException() {
        when(announcementRepository.existsById(99L)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> announcementService.deleteAnnouncement(99L)
        );

        assertEquals("Announcement not found", ex.getMessage());

        verify(announcementRepository).existsById(99L);
        verify(announcementRepository, never()).deleteById(anyLong());
    }

    @Test
    void getAllAnnouncements_returnsAnnouncementsInRepositoryOrder() {
        UserEntity teacher = new UserEntity();
        teacher.setEmail("teacher@test.com");
        teacher.setFullName("Teacher One");

        AnnouncementEntity first = new AnnouncementEntity();
        first.setTitle("First");
        first.setContent("First content");
        first.setCreatedBy(teacher);

        AnnouncementEntity second = new AnnouncementEntity();
        second.setTitle("Second");
        second.setContent("Second content");
        second.setCreatedBy(teacher);

        when(announcementRepository.findAllByOrderByCreatedAtDescIdDesc())
                .thenReturn(List.of(first, second));

        List<AnnouncementResponse> result = announcementService.getAllAnnouncements();

        assertEquals(2, result.size());
        assertEquals("First", result.get(0).title);
        assertEquals("Second", result.get(1).title);

        verify(announcementRepository).findAllByOrderByCreatedAtDescIdDesc();
    }
}