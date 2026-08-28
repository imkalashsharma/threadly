package com.threadly.auth.service.impl;

import com.threadly.auth.config.JwtProperties;
import com.threadly.auth.dto.request.LoginRequest;
import com.threadly.auth.dto.request.RegisterRequest;
import com.threadly.auth.dto.response.LoginResponse;
import com.threadly.auth.dto.response.UserResponse;
import com.threadly.auth.entity.RefreshToken;
import com.threadly.auth.entity.User;
import com.threadly.auth.entity.UserRole;
import com.threadly.auth.entity.UserStatus;
import com.threadly.auth.exception.InvalidCredentialsException;
import com.threadly.auth.exception.UserAlreadyExistsException;
import com.threadly.auth.repository.UserRepository;
import com.threadly.auth.security.JwtService;
import com.threadly.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();

        if(userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email is already registered.");
        }

        // create new user
        User user = new User();

        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        log.info("User registration successful: userId = {}", savedUser.getId());

        return new UserResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getRole(),
                savedUser.getStatus(),
                savedUser.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        log.debug("Authentication attempt for email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Authentication failed: user not found for email={}", email);

                    return new InvalidCredentialsException("Invalid email or password.");
                });

        if(!user.getStatus().equals(UserStatus.ACTIVE)) {
            log.warn("Authentication failed: inactive user, userId = {}, status = {}", user.getId(), user.getStatus());

            throw new InvalidCredentialsException("Invalid email or password.");
        }

        if(!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Authentication failed: invalid password, userId = {}", user.getId());

            throw new InvalidCredentialsException("Invalid email or password.");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);

        log.info("Authentication successful: userId = {}", user.getId());

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                900
        );
    }

    @Override
    @Transactional
    public LoginResponse refresh(String rawRefreshToken) {
        RefreshToken oldRefreshToken = refreshTokenService.getRefreshToken(rawRefreshToken);

        User user =  oldRefreshToken.getUser();
        String newRefreshToken = refreshTokenService.rotateRefreshToken(oldRefreshToken);

        String accessToken = jwtService.generateAccessToken(user);

        return new LoginResponse(
                accessToken,
                newRefreshToken,
                "Bearer",
                jwtProperties.expiration() / 1000
        );
    }

    @Override
    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revokeRefreshToken(rawRefreshToken);

        log.info("Logout successful.");
    }
}
