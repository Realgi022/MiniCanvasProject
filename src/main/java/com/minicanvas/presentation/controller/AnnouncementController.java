package com.minicanvas.presentation.controller;

import com.minicanvas.bll.services.AnnouncementService;
import com.minicanvas.presentation.dto.announcement.CreateAnnouncementRequest;
import com.minicanvas.presentation.dto.announcement.UpdateAnnouncementRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping
    public ResponseEntity<?> getAllAnnouncements() {
        return ResponseEntity.ok(announcementService.getAllAnnouncements());
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> createAnnouncement(@RequestBody CreateAnnouncementRequest request) {
        try {
            return ResponseEntity.ok(announcementService.createAnnouncement(request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new Object() {
                public final String error = ex.getMessage();
            });
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> updateAnnouncement(
            @PathVariable Long id,
            @RequestBody UpdateAnnouncementRequest request
    ) {
        try {
            return ResponseEntity.ok(announcementService.updateAnnouncement(id, request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new Object() {
                public final String error = ex.getMessage();
            });
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> deleteAnnouncement(@PathVariable Long id) {
        try {
            announcementService.deleteAnnouncement(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new Object() {
                public final String error = ex.getMessage();
            });
        }
    }
}