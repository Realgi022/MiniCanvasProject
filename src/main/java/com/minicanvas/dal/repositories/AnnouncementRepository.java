package com.minicanvas.dal.repositories;

import com.minicanvas.dal.entities.AnnouncementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<AnnouncementEntity, Long> {

    List<AnnouncementEntity> findAllByOrderByCreatedAtDescIdDesc();
}