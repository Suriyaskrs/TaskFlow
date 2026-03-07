package com.taskflow.dto.response;

import com.taskflow.model.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response body for project-related API responses.
 * Includes progress metrics computed from tasks.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private Long ownerId;

    // Progress metrics
    private int totalTasks;
    private int completedTasks;
    private double progressPercent;  // 0.0 – 100.0

    // Optionally include tasks list (used in detail views)
    private List<TaskResponse> tasks;

    /**
     * Factory — creates a basic ProjectResponse without tasks list.
     * Progress stats computed from counts.
     */
    public static ProjectResponse from(Project project, long totalTasks, long completedTasks) {
        double progress = totalTasks == 0 ? 0.0
                : Math.round((completedTasks * 100.0 / totalTasks) * 10.0) / 10.0;

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .createdAt(project.getCreatedAt())
                .ownerId(project.getUser().getId())
                .totalTasks((int) totalTasks)
                .completedTasks((int) completedTasks)
                .progressPercent(progress)
                .build();
    }
}