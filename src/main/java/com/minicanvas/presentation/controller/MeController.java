package com.minicanvas.presentation.controller;

import com.minicanvas.dal.repositories.UserRepository;
import com.minicanvas.presentation.dto.auth.MeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
public class MeController {

    private final UserRepository userRepository;

    public MeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body(new Object() {
                public final String error = "Unauthorized";
            });
        }

        var user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        var roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());

        return ResponseEntity.ok(
                new MeResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        roles
                )
        );
    }
}