package com.threadly.auth.controller;

import com.threadly.auth.entity.User;
import com.threadly.auth.entity.UserRole;
import com.threadly.auth.entity.UserStatus;
import com.threadly.auth.repository.RefreshTokenRepository;
import com.threadly.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16")
                    .withDatabaseName("auth_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void cleanDatabase() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        String email = "test-" + UUID.randomUUID() + "@email.com";
        String password = "test-password";

        String requestBody = """
                    {
                        "email": "%s",
                        "firstName": "test",
                        "lastName": "test",
                        "password": "%s"
                    }
                """.formatted(email, password);

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
                .andExpect(status().isCreated());

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        assertThat(user.getEmail().equals(email));
        assertThat(user.getPasswordHash()).isNotEqualTo(password);

        assertThat(passwordEncoder.matches(
                password,
                user.getPasswordHash()
        )).isTrue();
    }

    @Test
    void shouldRejectRegistrationWhenEmailAlreadyExists() throws Exception {
        String email = "test-" + UUID.randomUUID() + "@email.com";

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("pass@1224"));
        user.setFirstName("test");
        user.setLastName("test");
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        String requestBody = """
                    {
                        "email": "%s",
                        "firstName": "test",
                        "lastName": "test",
                        "password": "password@123"
                    }
                """.formatted(user.getEmail());

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isConflict());
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        String email = "test-" + UUID.randomUUID() + "@email.com";

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("password@123"));
        user.setFirstName("test");
        user.setLastName("test");
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        String requestBody = """
                    {
                        "email": "%s",
                        "password": "password@123"
                    }
                """.formatted(user.getEmail());

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void shouldRejectLoginWithInvalidPassword() throws Exception {
        String email = "test-" + UUID.randomUUID() + "@email.com";

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("password@123"));
        user.setFirstName("test");
        user.setLastName("test");
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        String requestBody = """
                    {
                        "email": "%s",
                        "password": "password@1"
                    }
                """.formatted(user.getEmail());

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRefreshSuccessfullyWithValidRefreshToken() throws Exception {
        String email = "test-" + UUID.randomUUID() + "@email.com";
        String password = "password@123";

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFirstName("test");
        user.setLastName("test");
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        // Login to obtain a valid refresh token
        String loginRequest = """
            {
                "email": "%s",
                "password": "%s"
            }
            """.formatted(email, password);

        MvcResult loginResult = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequest)
                )
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResponse);

        String refreshToken = loginJson
                .get("refreshToken")
                .asString();

        // Act
        String refreshRequest = """
            {
                "refreshToken": "%s"
            }
            """.formatted(refreshToken);

        // Assert
        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(refreshRequest)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void shouldRevokeRefreshTokenOnLogout() throws Exception {
        String email = "test-" + UUID.randomUUID() + "@email.com";
        String password = "password@123";

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFirstName("test");
        user.setLastName("test");
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        // Login to obtain a valid refresh token
        String loginRequest = """
            {
                "email": "%s",
                "password": "%s"
            }
            """.formatted(email, password);

        MvcResult loginResult = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequest)
                )
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResponse);

        String refreshToken = loginJson
                .get("refreshToken")
                .asString();

        // Act
        String logoutRequest = """
            {
                "refreshToken": "%s"
            }
            """.formatted(refreshToken);

        // Assert
        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(logoutRequest)
                )
                .andExpect(status().isOk());
    }
}
