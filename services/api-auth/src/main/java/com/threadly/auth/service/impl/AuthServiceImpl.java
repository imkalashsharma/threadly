package com.threadly.auth.service.impl;

import com.threadly.auth.dto.request.RegisterRequest;
import com.threadly.auth.dto.response.UserResponse;
import com.threadly.auth.entity.User;
import com.threadly.auth.entity.UserRole;
import com.threadly.auth.entity.UserStatus;
import com.threadly.auth.exception.UserAlreadyExistsException;
import com.threadly.auth.repository.UserRepository;
import com.threadly.auth.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
}
