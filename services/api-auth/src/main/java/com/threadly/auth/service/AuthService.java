package com.threadly.auth.service;

import com.threadly.auth.dto.request.LoginRequest;
import com.threadly.auth.dto.request.RefreshTokenRequest;
import com.threadly.auth.dto.request.RegisterRequest;
import com.threadly.auth.dto.response.LoginResponse;
import com.threadly.auth.dto.response.RefreshTokenResponse;
import com.threadly.auth.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest registerRequest);

    LoginResponse login(LoginRequest loginRequest);

    RefreshTokenResponse refresh(String rawRefreshToken);
}
