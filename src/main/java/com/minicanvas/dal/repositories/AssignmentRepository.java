package com.minicanvas.dal.repositories;

import com.minicanvas.dal.entities.AssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<AssignmentEntity, Long> {

    List<AssignmentEntity> findByClassEntityIdOrderByCreatedAtDescIdDesc(Long classId);

    List<AssignmentEntity> findByClassEntityIdInOrderByCreatedAtDescIdDesc(List<Long> classIds);
}