package org.example.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.example.backend.web.dto.chat.ChatHistoryItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
public class GeminiChatService {

    private static final String SYSTEM_CONTEXT = "You are the CertifyHub assistant. "
            + "You help with certificates, verification, exams, users, roles, themes, "
            + "email/SMS communications, and appointments. "
            + "Always answer in French, concise and professional. "
            + "If a question is outside CertifyHub, redirect politely to platform features.";

    private static final String MODEL_ACK = "Compris ! Je suis pret a vous assister sur CertifyHub."
            + " Comment puis-je vous aider ?";

    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Value("${app.gemini.enabled:false}")
    private boolean geminiEnabled;

    @Value("${app.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${app.gemini.model:gemini-2.0-flash}")
    private String geminiModel;

    public GeminiChatService(ObjectMapper objectMapper, RestClient.Builder restClientBuilder) {
        this.objectMapper = objectMapper;
        this.restClient = restClientBuilder.build();
    }

    public String chat(String message, List<ChatHistoryItem> history) {
        if (!geminiEnabled || !StringUtils.hasText(geminiApiKey)) {
            throw new IllegalStateException("Gemini is required but not configured. Set app.gemini.enabled=true and app.gemini.api-key.");
        }
        if (!StringUtils.hasText(message)) {
            throw new IllegalArgumentException("Message is required.");
        }

        List<Map<String, Object>> contents = new ArrayList<>();
        contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", SYSTEM_CONTEXT))));
        contents.add(Map.of("role", "model", "parts", List.of(Map.of("text", MODEL_ACK))));

        if (history != null) {
            for (ChatHistoryItem item : history) {
                if (item == null || !StringUtils.hasText(item.content())) {
                    continue;
                }
                String role = StringUtils.hasText(item.role()) ? item.role() : "user";
                contents.add(Map.of("role", role, "parts", List.of(Map.of("text", item.content()))));
            }
        }

        contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", message))));

        Map<String, Object> body = Map.of(
                "contents", contents,
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "maxOutputTokens", 1024
                )
        );

        try {
            String response = restClient.post()
                    .uri("https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={key}", geminiModel, geminiApiKey)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            String text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("");
            if (!StringUtils.hasText(text)) {
                throw new IllegalStateException("Gemini returned empty response.");
            }
            return text;
        } catch (Exception ex) {
            String details = ex.getMessage();
            throw new IllegalStateException("Gemini chat failed: " + details, ex);
        }
    }
}
