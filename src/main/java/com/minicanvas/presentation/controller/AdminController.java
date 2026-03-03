package com.minicanvas.presentation.controller;

import com.minicanvas.bll.services.AdminUserService;
import com.minicanvas.dal.entities.UserEntity;
import com.minicanvas.presentation.dto.CreateUserRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminUserService adminUserService;

    public AdminController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest req) {
        try {
            UserEntity created = adminUserService.createUser(req);

            // minimal response (don’t return passwordHash)
            return ResponseEntity.ok(new Object() {
                public final Long id = created.getId();
                public final String email = created.getEmail();
                public final String fullName = created.getFullName();
            });

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new Object() {
                public final String error = ex.getMessage();
            });
        }
    }
}