package com.taskflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body for POST /auth/login and POST /auth/register
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String type;
    private Long userId;
    private String name;
    private String email;

    public static AuthResponse of(String token, Long userId, String name, String email) {
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(userId)
                .name(name)
                .email(email)
                .build();
    }
}