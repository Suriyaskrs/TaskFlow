package com.taskflow.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration.
 *
 * Key decisions:
 *   - CSRF disabled (stateless REST API, no browser sessions)
 *   - Session stateless (JWT handles state — no server-side sessions)
 *   - Public endpoints: /auth/**, /swagger-ui/**, /h2-console/**
 *   - Everything else requires authentication
 *   - JwtFilter runs before Spring's built-in auth filter
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final AuthenticationProvider authenticationProvider;

    // Endpoints that don't require authentication
    private static final String[] PUBLIC_URLS = {
            "/auth/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/h2-console/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — not needed for stateless REST APIs
            .csrf(AbstractHttpConfigurer::disable)

            // Allow H2 console frames (dev only — blocked by X-Frame-Options by default)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(PUBLIC_URLS).permitAll()
                    .anyRequest().authenticated()
            )

            // Stateless session — no HttpSession created
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Use our custom AuthenticationProvider (DaoAuthenticationProvider)
            .authenticationProvider(authenticationProvider)

            // JWT filter runs before Spring's default username/password filter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}