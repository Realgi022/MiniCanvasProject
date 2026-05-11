package com.minicanvas.bll.services;

import com.minicanvas.dal.entities.ClassEntity;
import com.minicanvas.dal.entities.ClassMembershipEntity;
import com.minicanvas.dal.entities.UserEntity;
import com.minicanvas.dal.repositories.ClassMembershipRepository;
import com.minicanvas.dal.repositories.ClassRepository;
import com.minicanvas.dal.repositories.UserRepository;
import com.minicanvas.presentation.dto.classroom.AssignUserToClassRequest;
import com.minicanvas.presentation.dto.classroom.ClassMemberResponse;
import com.minicanvas.presentation.dto.classroom.ClassResponse;
import com.minicanvas.presentation.dto.classroom.ClassWithMembersResponse;
import com.minicanvas.presentation.dto.classroom.AssignableUserResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassroomService {

    private final ClassRepository classRepository;
    private final ClassMembershipRepository classMembershipRepository;
    private final UserRepository userRepository;

    public ClassroomService(
            ClassRepository classRepository,
            ClassMembershipRepository classMembershipRepository,
            UserRepository userRepository
    ) {
        this.classRepository = classRepository;
        this.classMembershipRepository = classMembershipRepository;
        this.userRepository = userRepository;
    }

    public List<ClassResponse> getAllClasses() {
        return classRepository.findAll()
                .stream()
                .map(classEntity -> new ClassResponse(
                        classEntity.getId(),
                        classEntity.getName()
                ))
                .toList();
    }

    public ClassWithMembersResponse getClassMembers(Long classId) {
        ClassEntity classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Class not found"));

        List<ClassMemberResponse> members = classMembershipRepository
                .findByClassEntityIdOrderByClassRoleAscUserFullNameAsc(classId)
                .stream()
                .map(this::toMemberResponse)
                .toList();

        return new ClassWithMembersResponse(
                classEntity.getId(),
                classEntity.getName(),
                members
        );
    }

    public List<ClassWithMembersResponse> getMyClasses() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        List<ClassMembershipEntity> myMemberships =
                classMembershipRepository.findByUserEmailOrderByClassEntityNameAsc(email);

        return myMemberships.stream()
                .map(membership -> getClassMembers(membership.getClassEntity().getId()))
                .toList();
    }

    public ClassMemberResponse assignUserToClass(AssignUserToClassRequest request) {
        validateAssignRequest(request);

        UserEntity user = userRepository.findById(request.userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ClassEntity classEntity = classRepository.findById(request.classId)
                .orElseThrow(() -> new IllegalArgumentException("Class not found"));

        ClassMembershipEntity membership = classMembershipRepository
                .findByClassEntityIdAndUserId(request.classId, request.userId)
                .orElseGet(ClassMembershipEntity::new);

        membership.setUser(user);
        membership.setClassEntity(classEntity);
        membership.setClassRole(request.classRole.trim().toUpperCase());

        ClassMembershipEntity saved = classMembershipRepository.save(membership);

        return toMemberResponse(saved);
    }

    public void removeUserFromClass(Long classId, Long userId) {
        ClassMembershipEntity membership = classMembershipRepository
                .findByClassEntityIdAndUserId(classId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User is not assigned to this class"));

        classMembershipRepository.delete(membership);
    }

    private void validateAssignRequest(AssignUserToClassRequest request) {
        if (request.userId == null) {
            throw new IllegalArgumentException("User id is required");
        }

        if (request.classId == null) {
            throw new IllegalArgumentException("Class id is required");
        }

        if (request.classRole == null || request.classRole.trim().isEmpty()) {
            throw new IllegalArgumentException("Class role is required");
        }

        String normalizedRole = request.classRole.trim().toUpperCase();

        if (!normalizedRole.equals("STUDENT") && !normalizedRole.equals("TEACHER")) {
            throw new IllegalArgumentException("Class role must be STUDENT or TEACHER");
        }
    }

    private ClassMemberResponse toMemberResponse(ClassMembershipEntity membership) {
        UserEntity user = membership.getUser();

        return new ClassMemberResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                membership.getClassRole()
        );
    }

    public List<AssignableUserResponse> getAssignableUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> {
                    String role = user.getRoles()
                            .stream()
                            .findFirst()
                            .map(r -> r.getName())
                            .orElse("NO_ROLE");

                    return new AssignableUserResponse(
                            user.getId(),
                            user.getEmail(),
                            user.getFullName(),
                            role
                    );
                })
                .toList();
    }
}