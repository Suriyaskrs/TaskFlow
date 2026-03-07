package com.taskflow.service;

import com.taskflow.dto.request.LoginRequest;
import com.taskflow.dto.request.RegisterRequest;
import com.taskflow.dto.response.AuthResponse;
import com.taskflow.exception.BadRequestException;
import com.taskflow.model.Role;
import com.taskflow.model.User;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles user registration and login.
 *
 * Registration:
 *   1. Check email uniqueness
 *   2. Hash password with BCrypt
 *   3. Save user
 *   4. Return JWT token
 *
 * Login:
 *   1. AuthenticationManager validates credentials
 *   2. Load user from DB
 *   3. Return JWT token
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user and returns a JWT token.
     * Throws BadRequestException if email already exists.
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", savedUser.getEmail());

        String token = jwtService.generateToken(savedUser);
        return AuthResponse.of(token, savedUser.getId(), savedUser.getName(), savedUser.getEmail());
    }

    /**
     * Authenticates a user and returns a JWT token.
     * AuthenticationManager throws BadCredentialsException on wrong password.
     */
    public AuthResponse login(LoginRequest request) {
        // This call verifies email + password via DaoAuthenticationProvider
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        log.info("User logged in: {}", user.getEmail());

        String token = jwtService.generateToken(user);
        return AuthResponse.of(token, user.getId(), user.getName(), user.getEmail());
    }
}