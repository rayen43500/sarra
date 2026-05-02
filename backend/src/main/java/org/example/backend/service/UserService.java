package org.example.backend.service;

import org.example.backend.domain.entity.Role;
import org.example.backend.domain.entity.User;
import org.example.backend.domain.enums.RoleName;
import org.example.backend.domain.enums.UserStatus;
import org.example.backend.repository.RoleRepository;
import org.example.backend.repository.UserRepository;
import org.example.backend.web.dto.user.AdminUpdateUserRequest;
import org.example.backend.web.dto.user.CreateUserRequest;
import org.example.backend.web.dto.user.UpdateProfileRequest;
import org.example.backend.web.dto.user.UserDto;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    public UserDto create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already used");
        }

        Role role = roleRepository.findByName(request.role() == null ? RoleName.ROLE_CLIENT : request.role()).orElseThrow();
        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus(request.status() == null ? UserStatus.ACTIVE : request.status());
        user.setRoles(Set.of(role));
        return toDto(userRepository.save(user));
    }

    public UserDto adminUpdate(Long userId, AdminUpdateUserRequest request) {
        User user = userRepository.findById(userId).orElseThrow();
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        if (request.status() != null) {
            user.setStatus(request.status());
        }
        return toDto(userRepository.save(user));
    }

    public void delete(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        userRepository.delete(user);
    }

    public UserDto setStatus(Long userId, UserStatus status) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setStatus(status);
        return toDto(userRepository.save(user));
    }

    public UserDto findById(Long id) {
        return toDto(userRepository.findById(id).orElseThrow());
    }

    public UserDto updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId).orElseThrow();
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        return toDto(userRepository.save(user));
    }

    public UserDto updateAvatar(Long userId, String avatarUrl) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setAvatarUrl(avatarUrl);
        return toDto(userRepository.save(user));
    }

    public void assignRole(Long userId, RoleName roleName) {
        User user = userRepository.findById(userId).orElseThrow();
        Role role = roleRepository.findByName(roleName).orElseThrow();
        user.setRoles(Set.of(role));
        userRepository.save(user);
    }

    public void resetPassword(Long userId, String rawPassword) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
    }

    private UserDto toDto(User user) {
        Set<String> roles = user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet());
        return new UserDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.getStatus().name(),
                roles
        );
    }
}
