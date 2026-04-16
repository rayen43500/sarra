package org.example.backend.service;

import org.example.backend.web.dto.auth.AuthResponse;
import org.example.backend.web.dto.auth.LoginRequest;
import org.example.backend.web.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
}
