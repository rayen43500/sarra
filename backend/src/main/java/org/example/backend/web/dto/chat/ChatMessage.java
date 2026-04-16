package org.example.backend.web.dto.chat;

public record ChatMessage(
        String sender,
        String content,
        String timestamp
) {
}
