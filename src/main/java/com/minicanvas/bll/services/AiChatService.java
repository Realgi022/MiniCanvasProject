package com.minicanvas.bll.services;

import com.minicanvas.dal.entities.AiChatMessageEntity;
import com.minicanvas.dal.entities.UserEntity;
import com.minicanvas.dal.repositories.AiChatMessageRepository;
import com.minicanvas.dal.repositories.UserRepository;
import com.minicanvas.presentation.dto.AiChatMessageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AiChatService {

    private final AiChatMessageRepository chatRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;

    public AiChatService(
            AiChatMessageRepository chatRepository,
            UserRepository userRepository,
            GeminiService geminiService
    ) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.geminiService = geminiService;
    }

    public List<AiChatMessageResponse> getChatHistory(String email) {
        UserEntity user = getUserByEmail(email);

        return chatRepository.findByUserIdOrderByCreatedAtAsc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public List<AiChatMessageResponse> sendMessage(String email, String message) {
        UserEntity user = getUserByEmail(email);

        if (message == null || message.trim().isEmpty()) {
            throw new RuntimeException("Message cannot be empty");
        }

        AiChatMessageEntity userMessage = new AiChatMessageEntity(
                user.getId(),
                "USER",
                message
        );

        chatRepository.save(userMessage);

        String aiAnswer = geminiService.askAi(message);

        AiChatMessageEntity aiMessage = new AiChatMessageEntity(
                user.getId(),
                "AI",
                aiAnswer
        );

        chatRepository.save(aiMessage);

        return getChatHistory(email);
    }

    @Transactional
    public void clearChat(String email) {
        UserEntity user = getUserByEmail(email);
        chatRepository.deleteByUserId(user.getId());
    }

    private UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private AiChatMessageResponse toResponse(AiChatMessageEntity message) {
        return new AiChatMessageResponse(
                message.getId(),
                message.getRole(),
                message.getMessage(),
                message.getCreatedAt()
        );
    }
}