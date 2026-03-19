package com.taskflow.controller;

import com.taskflow.dto.request.ProjectRequest;
import com.taskflow.dto.request.ProjectUpdateRequest;
import com.taskflow.dto.response.ApiResponse;
import com.taskflow.dto.response.ProjectResponse;
import com.taskflow.model.User;
import com.taskflow.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Project management endpoints.
 * All endpoints require JWT authentication.
 */
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project CRUD operations")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;

    /**
     * POST /projects
     * Create a new project for the authenticated user.
     */
    @PostMapping
    @Operation(summary = "Create a new project")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal User user) {

        ProjectResponse project = projectService.createProject(request, user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project created successfully", project));
    }

    /**
     * GET /projects
     * Get all projects belonging to the authenticated user.
     */
    @GetMapping
    @Operation(summary = "Get all projects for current user")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getAllProjects(
            @AuthenticationPrincipal User user) {

        List<ProjectResponse> projects = projectService.getUserProjects(user);
        return ResponseEntity.ok(ApiResponse.success("Projects retrieved", projects));
    }

    /**
     * GET /projects/{id}
     * Get a single project with its tasks list and progress.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID with tasks and progress")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        ProjectResponse project = projectService.getProjectById(id, user);
        return ResponseEntity.ok(ApiResponse.success("Project retrieved", project));
    }

    /**
     * PUT /projects/{id}
     * Update project name and/or description.
     * Partial update — only provided fields are changed.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update project name or description")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectUpdateRequest request,
            @AuthenticationPrincipal User user) {

        ProjectResponse project = projectService.updateProject(id, request, user);
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully", project));
    }

    /**
     * DELETE /projects/{id}
     * Delete a project and all its tasks.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a project and all its tasks")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        projectService.deleteProject(id, user);
        return ResponseEntity.ok(ApiResponse.success("Project deleted successfully"));
    }
}