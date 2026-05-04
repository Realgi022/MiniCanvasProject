package com.minicanvas.dal.repositories;

import com.minicanvas.dal.entities.AiChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiChatMessageRepository extends JpaRepository<AiChatMessageEntity, Long> {

    List<AiChatMessageEntity> findByUserIdOrderByCreatedAtAsc(Long userId);

    void deleteByUserId(Long userId);
}