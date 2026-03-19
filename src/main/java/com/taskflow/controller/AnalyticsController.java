package com.taskflow.controller;

import com.taskflow.dto.response.AnalyticsResponse;
import com.taskflow.dto.response.ApiResponse;
import com.taskflow.model.User;
import com.taskflow.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Analytics endpoints.
 *
 * GET /projects/{id}/analytics
 * Returns task breakdown by status, priority, overdue count,
 * and completion rate for a project.
 *
 * This is a strong interview talking point:
 *   "The system exposes a dedicated analytics layer that computes
 *    project health metrics using aggregated DB queries — no in-memory
 *    collection loading."
 */
@RestController
@RequestMapping("/projects/{projectId}/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Project analytics and progress metrics")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * GET /projects/{projectId}/analytics
     * Returns full project analytics:
     *   - Task counts by status (TODO, IN_PROGRESS, DONE)
     *   - Task counts by priority (HIGH, MEDIUM, LOW)
     *   - Overdue task count
     *   - Completion rate percentage
     */
    @GetMapping
    @Operation(summary = "Get analytics and progress metrics for a project")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getProjectAnalytics(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User user) {

        AnalyticsResponse analytics = analyticsService.getProjectAnalytics(projectId, user);
        return ResponseEntity.ok(ApiResponse.success("Analytics retrieved", analytics));
    }
}