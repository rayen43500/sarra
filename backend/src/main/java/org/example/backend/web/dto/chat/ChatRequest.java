package org.example.backend.web.dto.chat;

import java.util.List;

public record ChatRequest(
        String message,
        List<ChatHistoryItem> history
) {
}
