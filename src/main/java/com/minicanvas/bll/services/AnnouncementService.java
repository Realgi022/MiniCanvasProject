package com.minicanvas.bll.services;

import com.minicanvas.dal.entities.AnnouncementEntity;
import com.minicanvas.dal.entities.UserEntity;
import com.minicanvas.dal.repositories.AnnouncementRepository;
import com.minicanvas.dal.repositories.UserRepository;
import com.minicanvas.presentation.dto.announcement.AnnouncementResponse;
import com.minicanvas.presentation.dto.announcement.CreateAnnouncementRequest;
import com.minicanvas.presentation.dto.announcement.UpdateAnnouncementRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;

    public AnnouncementService(
            AnnouncementRepository announcementRepository,
            UserRepository userRepository
    ) {
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
    }

    public List<AnnouncementResponse> getAllAnnouncements() {
        return announcementRepository.findAllByOrderByCreatedAtDescIdDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public AnnouncementResponse createAnnouncement(CreateAnnouncementRequest request) {
        validateRequest(request.title, request.content);

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        UserEntity teacher = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

        AnnouncementEntity announcement = new AnnouncementEntity();
        announcement.setTitle(request.title.trim());
        announcement.setContent(request.content.trim());
        announcement.setCreatedBy(teacher);

        AnnouncementEntity saved = announcementRepository.save(announcement);

        return toResponse(saved);
    }

    public AnnouncementResponse updateAnnouncement(Long id, UpdateAnnouncementRequest request) {
        validateRequest(request.title, request.content);

        AnnouncementEntity announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Announcement not found"));

        announcement.setTitle(request.title.trim());
        announcement.setContent(request.content.trim());

        AnnouncementEntity saved = announcementRepository.save(announcement);

        return toResponse(saved);
    }

    public void deleteAnnouncement(Long id) {
        if (!announcementRepository.existsById(id)) {
            throw new IllegalArgumentException("Announcement not found");
        }

        announcementRepository.deleteById(id);
    }

    private void validateRequest(String title, String content) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content is required");
        }
    }

    private AnnouncementResponse toResponse(AnnouncementEntity announcement) {
        UserEntity createdBy = announcement.getCreatedBy();

        return new AnnouncementResponse(
                announcement.getId(),
                announcement.getTitle(),
                announcement.getContent(),
                createdBy.getEmail(),
                createdBy.getFullName(),
                announcement.getCreatedAt(),
                announcement.getUpdatedAt()
        );
    }
}