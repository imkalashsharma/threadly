package com.threadly.auth.dto.response;

public record RefreshTokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}
