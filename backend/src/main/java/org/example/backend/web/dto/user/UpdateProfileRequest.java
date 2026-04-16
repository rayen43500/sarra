package org.example.backend.web.dto.user;

public record UpdateProfileRequest(
        String firstName,
        String lastName,
        String phone
) {
}
