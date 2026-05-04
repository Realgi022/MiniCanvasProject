package com.minicanvas.bll.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private final String apiKey;
    private final String apiUrl;
    private final RestClient restClient;

    public GeminiService(
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.api.url}") String apiUrl
    ) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.restClient = RestClient.create();
    }

    public String testConfig() {
        if (apiKey == null || apiKey.isBlank()) {
            return "Gemini API key is missing";
        }

        return "Gemini API key loaded successfully. API URL: " + apiUrl;
    }

    public String askAi(String question) {
        String prompt = """
                You are an AI study assistant inside a school app called MiniCanvas.

                Your job:
                - Help students understand school topics.
                - Explain programming concepts clearly.
                - Help with studying, assignments, planning, and revision.
                - Use simple language.
                - Give step-by-step explanations when useful.

                If the question is not related to school, studying, programming, or learning,
                politely guide the user back to study-related help.

                Student question:
                """ + question;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );

        try {
            Map response = restClient.post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            return extractText(response);
        } catch (Exception e) {
            e.printStackTrace();

            String errorMessage = e.getMessage();

            if (errorMessage != null && errorMessage.contains("503")) {
                return "The AI service is busy right now. Please try again in a moment.";
            }

            if (errorMessage != null && errorMessage.contains("429")) {
                return "The AI service rate limit was reached. Please wait a little and try again.";
            }

            return "Sorry, I could not reach the AI service right now.";
        }
    }

    private String extractText(Map response) {
        try {
            List candidates = (List) response.get("candidates");
            Map firstCandidate = (Map) candidates.get(0);

            Map content = (Map) firstCandidate.get("content");
            List parts = (List) content.get("parts");
            Map firstPart = (Map) parts.get(0);

            return firstPart.get("text").toString();
        } catch (Exception e) {
            return "Sorry, I could not read the AI response.";
        }
    }
}