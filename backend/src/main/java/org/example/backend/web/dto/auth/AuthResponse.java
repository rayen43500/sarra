package org.example.backend.web.dto.auth;

public record AuthResponse(
        String token,
        String email,
        String role
) {
}
