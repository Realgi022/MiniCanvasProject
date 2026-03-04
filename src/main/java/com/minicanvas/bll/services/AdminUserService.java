package com.minicanvas.bll.services;

import com.minicanvas.dal.entities.RoleEntity;
import com.minicanvas.dal.entities.UserEntity;
import com.minicanvas.dal.repositories.RoleRepository;
import com.minicanvas.dal.repositories.UserRepository;
import com.minicanvas.presentation.dto.CreateUserRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        user.setRoles(Set.of(role));

        return userRepository.save(user);
    }
}