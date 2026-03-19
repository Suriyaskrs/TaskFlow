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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles user registration and login.
 *
 * Day 4 addition:
 *   POST /auth/register-admin?adminSecret=xxx
 *   Registers an ADMIN user only if the correct admin secret is provided.
 *   The secret is set in application.properties (admin.secret).
 *   This prevents anyone from self-promoting to ADMIN.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${admin.secret}")
    private String adminSecret;

    // -------------------------------------------------------
    // Register normal user
    // -------------------------------------------------------

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

        User saved = userRepository.save(user);
        log.info("New user registered: {}", saved.getEmail());

        String token = jwtService.generateToken(saved);
        return AuthResponse.of(token, saved.getId(), saved.getName(), saved.getEmail());
    }

    // -------------------------------------------------------
    // Register admin user (requires secret key)
    // -------------------------------------------------------

    /**
     * Registers an admin user.
     * Requires the correct adminSecret query param — set in application.properties.
     * Without the secret, no one can create an admin account.
     */
    public AuthResponse registerAdmin(RegisterRequest request, String providedSecret) {
        if (!adminSecret.equals(providedSecret)) {
            throw new BadRequestException("Invalid admin secret. Admin registration denied.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ADMIN)
                .build();

        User saved = userRepository.save(user);
        log.info("New ADMIN registered: {}", saved.getEmail());

        String token = jwtService.generateToken(saved);
        return AuthResponse.of(token, saved.getId(), saved.getName(), saved.getEmail());
    }

    // -------------------------------------------------------
    // Login
    // -------------------------------------------------------

    public AuthResponse login(LoginRequest request) {
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