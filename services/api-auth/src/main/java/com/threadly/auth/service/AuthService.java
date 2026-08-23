package com.threadly.auth.service;

import com.threadly.auth.dto.request.LoginRequest;
import com.threadly.auth.dto.request.RegisterRequest;
import com.threadly.auth.dto.response.AuthResponse;
import com.threadly.auth.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest registerRequest);

    AuthResponse login(LoginRequest loginRequest);
}
