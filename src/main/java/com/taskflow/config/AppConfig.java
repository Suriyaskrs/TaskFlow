package com.taskflow.config;

import com.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Application-level Spring beans.
 *
 * Defines:
 *   - UserDetailsService: loads user by email from DB
 *   - PasswordEncoder: BCrypt (industry standard)
 *   - AuthenticationProvider: wires UserDetailsService + PasswordEncoder
 *   - AuthenticationManager: used in AuthService to perform login
 */
@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private final UserRepository userRepository;

    /**
     * Loads user by email (our login identifier).
     * Spring Security calls this during authentication.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * BCrypt password encoder.
     * Strength 10 (default) — suitable for production.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * DaoAuthenticationProvider wires together:
     *   - UserDetailsService (how to load the user)
     *   - PasswordEncoder (how to verify the password)
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AuthenticationManager used in AuthService.authenticate() call during login.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}