package com.taskflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * System-wide stats returned by GET /admin/stats
 * Only accessible by ADMIN role.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {

    private long totalUsers;
    private long totalProjects;
    private long totalTasks;
    private long totalCompletedTasks;
    private long totalOverdueTasks;
    private double overallCompletionRate;  // across all projects
}