package com.threadly.auth.service;

import com.threadly.auth.entity.RefreshToken;
import com.threadly.auth.entity.User;
import com.threadly.auth.repository.RefreshTokenRepository;
import com.threadly.auth.service.impl.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void shouldRevokeRefreshTokenSuccessfully() {
        String rawToken = "some-refresh-token";
        String tokenHash = "hashed-token";

        User user = new User();
        RefreshToken refreshToken = new RefreshToken(user, tokenHash, Instant.now().plusSeconds(3600));

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(refreshToken));

        // act
        refreshTokenService.revokeRefreshToken(rawToken);

        // assert
        assertThat(refreshToken.isRevoked()).isTrue();
    }
}
