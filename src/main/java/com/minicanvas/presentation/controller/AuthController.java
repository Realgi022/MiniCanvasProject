package com.minicanvas.presentation.controller;

import com.minicanvas.bll.services.AuthService;
import com.minicanvas.dal.repositories.UserRepository;
import com.minicanvas.presentation.dto.auth.LoginRequest;
import com.minicanvas.presentation.dto.auth.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            String token = authService.login(req.email, req.password);
            var user = userRepository.findByEmail(req.email.trim().toLowerCase()).orElseThrow();
            var roles = user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet());
            return ResponseEntity.ok(new LoginResponse(token, user.getEmail(), roles));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(401).body(new Object() { public final String error = ex.getMessage(); });
        }
    }
}