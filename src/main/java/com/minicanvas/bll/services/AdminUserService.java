package com.minicanvas.bll.services;

import com.minicanvas.dal.entities.RoleEntity;
import com.minicanvas.dal.entities.UserEntity;
import com.minicanvas.dal.repositories.RoleRepository;
import com.minicanvas.dal.repositories.UserRepository;
import com.minicanvas.presentation.dto.CreateUserRequest;
import com.minicanvas.presentation.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.minicanvas.presentation.dto.UserResponse;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashSet;

import java.util.Set;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //Validates required fields
    public UserEntity createUser(CreateUserRequest req) {
        if (req.email == null || req.email.isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        if (req.password == null || req.password.isBlank()) {
            throw new IllegalArgumentException("password is required");
        }
        if (req.role == null || req.role.isBlank()) {
            throw new IllegalArgumentException("role is required");
        }

        String roleName = req.role.trim().toUpperCase();

        if (!roleName.equals("TEACHER") && !roleName.equals("STUDENT")) {
            throw new IllegalArgumentException("role must be TEACHER or STUDENT");
        }

        if (userRepository.existsByEmail(req.email.trim().toLowerCase())) {
            throw new IllegalArgumentException("email already exists");
        }

        RoleEntity role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("role not found in DB: " + roleName));

        UserEntity user = new UserEntity();
        user.setEmail(req.email.trim().toLowerCase());
        user.setFullName(req.fullName);
        user.setPasswordHash(passwordEncoder.encode(req.password));
        user.setEnabled(true);
        user.setRoles(new HashSet<>(Set.of(role)));
        return userRepository.save(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getEnabled(),
                        user.getRoles().stream()
                                .findFirst()
                                .map(RoleEntity::getName)
                                .orElse("NO_ROLE")
                ))
                .collect(Collectors.toList());
    }

    public UserResponse updateUserRole(Long userId, String newRole) {
        if (newRole == null || newRole.isBlank()) {
            throw new IllegalArgumentException("role is required");
        }

        String roleName = newRole.trim().toUpperCase();

        if (!roleName.equals("TEACHER") && !roleName.equals("STUDENT")) {
            throw new IllegalArgumentException("role must be TEACHER or STUDENT");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        RoleEntity role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("role not found in DB: " + roleName));

        user.setRoles(new HashSet<>(Set.of(role)));

        UserEntity saved = userRepository.save(user);

        return new UserResponse(
                saved.getId(),
                saved.getFullName(),
                saved.getEmail(),
                saved.getEnabled(),
                saved.getRoles().stream()
                        .findFirst()
                        .map(RoleEntity::getName)
                        .orElse("NO_ROLE")
        );
    }
}