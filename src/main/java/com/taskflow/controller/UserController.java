package com.taskflow.controller;

import com.taskflow.dto.response.AnalyticsResponse;
import com.taskflow.dto.response.ApiResponse;
import com.taskflow.dto.response.UserResponse;
import com.taskflow.model.User;
import com.taskflow.service.AnalyticsService;
import com.taskflow.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * User profile and account management endpoints.
 *
 * Day 3 additions:
 *   - DELETE /users/me  (delete account with password confirmation)
 *   - GET /projects/{id}/analytics  moved to AnalyticsController
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile and account management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    /**
     * GET /users/me
     * Returns current user's profile (no password exposed).
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(
                ApiResponse.success("Profile retrieved", UserResponse.from(user)));
    }

    /**
     * DELETE /users/me?confirmPassword=yourpassword
     * Permanently deletes the user account and all their data.
     * Requires password confirmation as a safety measure.
     */
    @DeleteMapping("/me")
    @Operation(summary = "Delete user account (requires password confirmation)")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @RequestParam String confirmPassword,
            @AuthenticationPrincipal User user) {

        userService.deleteAccount(user, confirmPassword);
        return ResponseEntity.ok(
                ApiResponse.success("Account deleted successfully. All data has been removed."));
    }
}