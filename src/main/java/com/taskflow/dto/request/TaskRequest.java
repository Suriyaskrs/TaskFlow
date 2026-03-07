package com.taskflow.dto.request;

import com.taskflow.model.Priority;
import com.taskflow.model.TaskStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request body for:
 *   POST /projects/{id}/tasks  (create task)
 *   PUT  /tasks/{id}           (update task)
 *
 * All fields optional for update — service handles partial updates.
 * 'deadline' must be a future date (enforced by @Future).
 */
@Data
public class TaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private TaskStatus status;

    private Priority priority;

    @Future(message = "Deadline must be a future date")
    private LocalDate deadline;
}