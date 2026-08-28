package com.threadly.auth.service.impl;

import com.threadly.auth.entity.RefreshToken;
import com.threadly.auth.entity.User;
import com.threadly.auth.exception.InvalidCredentialsException;
import com.threadly.auth.repository.RefreshTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Slf4j
@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    private static final Duration REFRESH_TOKEN_EXPIRATION = Duration.ofDays(30);

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String createRefreshToken(User user) {
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        String tokenHash = hashToken(rawToken); // token hash to keep in db
        
        // create refresh token
        RefreshToken refreshToken = new RefreshToken(user, tokenHash, Instant.now().plus(REFRESH_TOKEN_EXPIRATION));
        
        // save in db
        refreshTokenRepository.save(refreshToken);
        refreshTokenRepository.flush();

        log.info("Refresh token successfully generated.");
        return rawToken;
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return Base64.getEncoder()
                    .encodeToString(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    public RefreshToken getRefreshToken(String rawToken) {
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> {
                    log.warn("Invalid refresh token.");

                    return new InvalidCredentialsException("Invalid refresh token.");
                });

        if(refreshToken.isExpired()) {
            log.warn("Refresh token is expired.");

            throw new  InvalidCredentialsException("Refresh token is expired.");
        }

        if(refreshToken.isRevoked()) {
            log.warn("Refresh token is revoked.");

            throw new InvalidCredentialsException("Refresh token is revoked.");
        }

        log.info("Refresh token successfully generated.");

        return refreshToken;
    }

    public String rotateRefreshToken(RefreshToken oldToken) {
        oldToken.revoke();
        log.info("Old refresh token is revoked.");

        return createRefreshToken(oldToken.getUser());
    }

    public void revokeRefreshToken(String rawRefreshToken) {
        RefreshToken refreshToken = getRefreshToken(rawRefreshToken);
        refreshToken.revoke();

        log.info("Refresh token successfully revoked.");
    }
}
