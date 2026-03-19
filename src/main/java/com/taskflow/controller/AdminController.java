package com.taskflow.controller;

import com.taskflow.dto.response.AdminStatsResponse;
import com.taskflow.dto.response.ApiResponse;
import com.taskflow.dto.response.PagedResponse;
import com.taskflow.dto.response.ProjectResponse;
import com.taskflow.dto.response.UserResponse;
import com.taskflow.model.User;
import com.taskflow.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-only endpoints.
 *
 * Double-locked security:
 *   1. SecurityConfig blocks non-ADMIN at URL level (/admin/**)
 *   2. @PreAuthorize("hasRole('ADMIN')") on every method as backup
 *
 * Endpoints:
 *   GET    /admin/stats               — system-wide stats
 *   GET    /admin/users               — all users (paginated)
 *   DELETE /admin/users/{id}          — delete any user + their data
 *   GET    /admin/projects            — all projects (paginated)
 *   DELETE /admin/projects/{id}       — delete any project + tasks
 *   DELETE /admin/tasks/{id}          — delete any single task
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin-only management endpoints (requires ADMIN role)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")   // applies to all methods in this controller
public class AdminController {

    private final AdminService adminService;

    // -------------------------------------------------------
    // System stats
    // -------------------------------------------------------

    /**
     * GET /admin/stats
     * System-wide aggregate stats: total users, projects, tasks,
     * completed tasks, overdue tasks, overall completion rate.
     */
    @GetMapping("/stats")
    @Operation(summary = "Get system-wide stats (admin only)")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getSystemStats() {
        return ResponseEntity.ok(
                ApiResponse.success("System stats retrieved", adminService.getSystemStats()));
    }

    // -------------------------------------------------------
    // User management
    // -------------------------------------------------------

    /**
     * GET /admin/users?page=0&size=20
     * List all registered users with their project counts.
     */
    @GetMapping("/users")
    @Operation(summary = "List all users (admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved", adminService.getAllUsers(page, size)));
    }

    /**
     * DELETE /admin/users/{id}
     * Permanently deletes a user account and ALL their projects and tasks.
     * Admin cannot delete their own account via this endpoint.
     */
    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete any user by ID (admin only) — cascades to all their data")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal User adminUser) {

        adminService.deleteUserById(id, adminUser);
        return ResponseEntity.ok(
                ApiResponse.success("User id=" + id + " and all their data deleted successfully"));
    }

    // -------------------------------------------------------
    // Project management
    // -------------------------------------------------------

    /**
     * GET /admin/projects?page=0&size=20
     * List all projects across all users with progress stats.
     */
    @GetMapping("/projects")
    @Operation(summary = "List all projects across all users (admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<ProjectResponse>>> getAllProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                ApiResponse.success("Projects retrieved", adminService.getAllProjects(page, size)));
    }

    /**
     * DELETE /admin/projects/{id}
     * Deletes any project and all its tasks regardless of owner.
     */
    @DeleteMapping("/projects/{id}")
    @Operation(summary = "Delete any project by ID (admin only) — cascades to all tasks")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {
        adminService.deleteProjectById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Project id=" + id + " and all its tasks deleted successfully"));
    }

    // -------------------------------------------------------
    // Task management
    // -------------------------------------------------------

    /**
     * DELETE /admin/tasks/{id}
     * Deletes any single task by ID regardless of which project it belongs to.
     */
    @DeleteMapping("/tasks/{id}")
    @Operation(summary = "Delete any task by ID (admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long id) {
        adminService.deleteTaskById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Task id=" + id + " deleted successfully"));
    }
}