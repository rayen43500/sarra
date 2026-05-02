package org.example.backend.web.dto.user;

import java.util.Set;

public record UserDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String avatarUrl,
        String status,
        Set<String> roles
) {
}
