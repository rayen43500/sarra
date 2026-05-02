package org.example.backend.service;

import org.example.backend.domain.entity.User;
import org.example.backend.repository.UserRepository;
import java.security.Principal;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User require(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new IllegalArgumentException("Missing authenticated user");
        }
        return userRepository.findByEmail(principal.getName()).orElseThrow();
    }
}
