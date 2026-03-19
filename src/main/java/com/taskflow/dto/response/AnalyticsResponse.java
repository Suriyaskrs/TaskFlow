package com.taskflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Analytics summary for a project.
 * Returned by GET /projects/{id}/analytics
 *
 * Shows task breakdown by status and priority,
 * overdue count, and completion rate.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {

    private Long projectId;
    private String projectName;

    // Task counts by status
    private long totalTasks;
    private long todoTasks;
    private long inProgressTasks;
    private long doneTasks;

    // Task counts by priority
    private long highPriorityTasks;
    private long mediumPriorityTasks;
    private long lowPriorityTasks;

    // Derived metrics
    private long overdueTasks;
    private double completionRate;   // 0.0 – 100.0
}