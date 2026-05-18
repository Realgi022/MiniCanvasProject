package com.minicanvas.presentation.controller;

import com.minicanvas.bll.services.AssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/grades")
public class GradeController {

    private final AssignmentService assignmentService;

    public GradeController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyGrades() {
        return ResponseEntity.ok(assignmentService.getMyGrades());
    }
}