package com.minicanvas.presentation.controller;

import com.minicanvas.bll.services.AiChatService;
import com.minicanvas.presentation.dto.AiChatMessageResponse;
import com.minicanvas.presentation.dto.AiChatRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai/chat")
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @GetMapping
    public List<AiChatMessageResponse> getChat(Authentication authentication) {
        String email = authentication.getName();
        return aiChatService.getChatHistory(email);
    }

    @PostMapping
    public List<AiChatMessageResponse> sendMessage(
            Authentication authentication,
            @RequestBody AiChatRequest request
    ) {
        String email = authentication.getName();
        return aiChatService.sendMessage(email, request.getMessage());
    }

    @DeleteMapping
    public void clearChat(Authentication authentication) {
        String email = authentication.getName();
        aiChatService.clearChat(email);
    }
}