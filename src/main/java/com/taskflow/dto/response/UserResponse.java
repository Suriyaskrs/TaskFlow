package com.taskflow.dto.response;

import com.taskflow.model.Role;
import com.taskflow.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for user profile.
 * Never expose the User entity directly (contains password hash).
 *
 * Day 4: added projectCount field (populated in admin views).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private long projectCount;   // 0 for normal profile, populated in admin list

    /**
     * Basic profile — no project count (used in /users/me).
     */
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .projectCount(0)
                .build();
    }

    /**
     * Admin view — includes project count.
     */
    public static UserResponse from(User user, long projectCount) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .projectCount(projectCount)
                .build();
    }
}