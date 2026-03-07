package com.taskflow.dto.response;

import com.taskflow.model.Priority;
import com.taskflow.model.Task;
import com.taskflow.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response body for task-related API responses.
 * Converts Task entity → DTO (never expose entity directly to client).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private LocalDate deadline;
    private Long projectId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean overdue;

    /**
     * Static factory — converts Task entity to TaskResponse DTO.
     */
    public static TaskResponse from(Task task) {
        boolean isOverdue = task.getDeadline() != null
                && task.getDeadline().isBefore(LocalDate.now())
                && task.getStatus() != TaskStatus.DONE;

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .deadline(task.getDeadline())
                .projectId(task.getProject().getId())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .overdue(isOverdue)
                .build();
    }
}