package org.example.backend.web.dto.user;

import org.example.backend.domain.enums.UserStatus;

public record AdminUpdateUserRequest(
        String firstName,
        String lastName,
        String phone,
        UserStatus status
) {
}
