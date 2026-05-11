package com.minicanvas.dal.repositories;

import com.minicanvas.dal.entities.ClassMembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassMembershipRepository extends JpaRepository<ClassMembershipEntity, Long> {

    List<ClassMembershipEntity> findByClassEntityIdOrderByClassRoleAscUserFullNameAsc(Long classId);

    List<ClassMembershipEntity> findByUserEmailOrderByClassEntityNameAsc(String email);

    Optional<ClassMembershipEntity> findByClassEntityIdAndUserId(Long classId, Long userId);

    boolean existsByClassEntityIdAndUserId(Long classId, Long userId);
}