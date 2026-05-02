package org.example.backend.web.dto.social;

public record SocialProvidersDto(
        String facebookAuthUrl,
        String linkedInAuthUrl,
        String note
) {
}
