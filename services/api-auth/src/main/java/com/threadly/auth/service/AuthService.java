package com.threadly.auth.service;

import com.threadly.auth.dto.request.LoginRequest;
import com.threadly.auth.dto.request.RegisterRequest;
import com.threadly.auth.dto.response.LoginResponse;
import com.threadly.auth.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest registerRequest);

    LoginResponse login(LoginRequest loginRequest);

    LoginResponse refresh(String rawRefreshToken);

    void logout(String rawRefreshToken);
}
