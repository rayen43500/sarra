package org.example.backend.web.dto.chat;

public record ChatHistoryItem(
        String role,
        String content
) {
}
