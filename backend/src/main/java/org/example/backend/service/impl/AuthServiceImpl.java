package org.example.backend.service.impl;

import org.example.backend.domain.entity.Role;
import org.example.backend.domain.entity.User;
import org.example.backend.domain.enums.RoleName;
import org.example.backend.domain.enums.UserStatus;
import org.example.backend.repository.RoleRepository;
import org.example.backend.repository.UserRepository;
import org.example.backend.security.CustomUserDetailsService;
import org.example.backend.security.JwtService;
import org.example.backend.service.AuthService;
import org.example.backend.web.dto.auth.AuthResponse;
import org.example.backend.web.dto.auth.LoginRequest;
import org.example.backend.web.dto.auth.RegisterRequest;
import java.util.Map;
import java.util.Set;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            CustomUserDetailsService customUserDetailsService,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email()).orElseThrow();
        String role = user.getRoles().stream().findFirst().map(r -> r.getName().name()).orElse(RoleName.ROLE_CLIENT.name());
        String token = jwtService.generateToken(
                customUserDetailsService.loadUserByUsername(request.email()),
                Map.of("role", role)
        );

        return new AuthResponse(token, user.getEmail(), role);
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already used");
        }

        Role clientRole = roleRepository.findByName(RoleName.ROLE_CLIENT).orElseThrow();

        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(Set.of(clientRole));
        userRepository.save(user);

        String token = jwtService.generateToken(customUserDetailsService.loadUserByUsername(user.getEmail()));
        return new AuthResponse(token, user.getEmail(), RoleName.ROLE_CLIENT.name());
    }
}
