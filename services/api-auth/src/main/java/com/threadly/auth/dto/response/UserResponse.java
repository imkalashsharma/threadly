package com.threadly.auth.dto.response;

import com.threadly.auth.entity.UserRole;
import com.threadly.auth.entity.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        UserRole role,
        UserStatus status,
        Instant createdAt
) {}
