package com.threadly.auth.controller;

import com.threadly.auth.dto.request.LoginRequest;
import com.threadly.auth.dto.request.RegisterRequest;
import com.threadly.auth.dto.response.AuthResponse;
import com.threadly.auth.dto.response.UserResponse;
import com.threadly.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        UserResponse userResponse = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<String> me(Authentication authentication) {
        return ResponseEntity.ok("Authenticated user: " + authentication.getName());
    }
}
