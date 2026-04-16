package org.example.backend.service;

import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class BadWordsFilterService {

    private final List<String> blockedWords = List.of(
            "idiot",
            "stupid",
            "hate",
            "dummy",
            "nul"
    );

    public boolean containsBadWords(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String normalized = text.toLowerCase(Locale.ROOT);
        return blockedWords.stream().anyMatch(normalized::contains);
    }

    public String sanitize(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        String sanitized = text;
        for (String word : blockedWords) {
            sanitized = sanitized.replaceAll("(?i)" + java.util.regex.Pattern.quote(word), "***");
        }
        return sanitized;
    }
}
