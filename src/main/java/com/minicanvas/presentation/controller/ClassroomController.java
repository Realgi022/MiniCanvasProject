package com.minicanvas.presentation.controller;

import com.minicanvas.bll.services.ClassroomService;
import com.minicanvas.presentation.dto.classroom.AssignUserToClassRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/classes")
public class ClassroomController {

    private final ClassroomService classroomService;

    public ClassroomController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getAllClasses() {
        return ResponseEntity.ok(classroomService.getAllClasses());
    }

    @GetMapping("/{classId}/members")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getClassMembers(@PathVariable Long classId) {
        try {
            return ResponseEntity.ok(classroomService.getClassMembers(classId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new Object() {
                public final String error = ex.getMessage();
            });
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyClasses() {
        return ResponseEntity.ok(classroomService.getMyClasses());
    }

    @PostMapping("/assign")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> assignUserToClass(@RequestBody AssignUserToClassRequest request) {
        try {
            return ResponseEntity.ok(classroomService.assignUserToClass(request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new Object() {
                public final String error = ex.getMessage();
            });
        }
    }

    @DeleteMapping("/{classId}/members/{userId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> removeUserFromClass(
            @PathVariable Long classId,
            @PathVariable Long userId
    ) {
        try {
            classroomService.removeUserFromClass(classId, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new Object() {
                public final String error = ex.getMessage();
            });
        }
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getAssignableUsers() {
        return ResponseEntity.ok(classroomService.getAssignableUsers());
    }
}