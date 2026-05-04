package com.minicanvas.presentation.controller;

import com.minicanvas.bll.services.GeminiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GeminiTestController {

    private final GeminiService geminiService;

    public GeminiTestController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @GetMapping("/api/gemini/test")
    public String testGeminiConfig() {
        return geminiService.testConfig();
    }

    @GetMapping("/api/gemini/ask-test")
    public String askTest() {
        return geminiService.askAi("Explain the SOLID principles in simple words.");
    }
}