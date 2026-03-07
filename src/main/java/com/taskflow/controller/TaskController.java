package com.taskflow.controller;

import com.taskflow.dto.request.TaskRequest;
import com.taskflow.dto.response.ApiResponse;
import com.taskflow.dto.response.TaskResponse;
import com.taskflow.model.Priority;
import com.taskflow.model.User;
import com.taskflow.service.TaskService;
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
 * Task management endpoints.
 *
 * URL structure:
 *   Tasks live inside projects:  /projects/{projectId}/tasks
 *   Task-specific operations:    /projects/{projectId}/tasks/{taskId}
 *
 * This nested structure makes the ownership clear in the URL itself.
 * It also means we always know which project a task belongs to.
 */
@RestController
@RequestMapping("/projects/{projectId}/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task CRUD operations")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    /**
     * POST /projects/{projectId}/tasks
     * Create a new task in the specified project.
     */
    @PostMapping
    @Operation(summary = "Create a task in a project")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal User user) {

        TaskResponse task = taskService.createTask(projectId, request, user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully", task));
    }

    /**
     * GET /projects/{projectId}/tasks
     * Get all tasks for a project.
     */
    @GetMapping
    @Operation(summary = "Get all tasks in a project")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasks(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User user) {

        List<TaskResponse> tasks = taskService.getTasksByProject(projectId, user);
        return ResponseEntity.ok(ApiResponse.success("Tasks retrieved", tasks));
    }

    /**
     * GET /projects/{projectId}/tasks/priority/{priority}
     * Get tasks filtered by priority (LOW, MEDIUM, HIGH).
     */
    @GetMapping("/priority/{priority}")
    @Operation(summary = "Get tasks by priority")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByPriority(
            @PathVariable Long projectId,
            @PathVariable Priority priority,
            @AuthenticationPrincipal User user) {

        List<TaskResponse> tasks = taskService.getTasksByPriority(projectId, priority, user);
        return ResponseEntity.ok(ApiResponse.success("Tasks retrieved by priority", tasks));
    }

    /**
     * GET /projects/{projectId}/tasks/overdue
     * Get all overdue tasks (deadline passed, not yet DONE).
     */
    @GetMapping("/overdue")
    @Operation(summary = "Get overdue tasks in a project")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getOverdueTasks(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User user) {

        List<TaskResponse> tasks = taskService.getOverdueTasks(projectId, user);
        return ResponseEntity.ok(ApiResponse.success("Overdue tasks retrieved", tasks));
    }

    /**
     * PUT /projects/{projectId}/tasks/{taskId}
     * Update a task (partial update — only provided fields are changed).
     */
    @PutMapping("/{taskId}")
    @Operation(summary = "Update a task")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestBody TaskRequest request,
            @AuthenticationPrincipal User user) {

        TaskResponse task = taskService.updateTask(projectId, taskId, request, user);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", task));
    }

    /**
     * DELETE /projects/{projectId}/tasks/{taskId}
     * Delete a specific task.
     */
    @DeleteMapping("/{taskId}")
    @Operation(summary = "Delete a task")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @AuthenticationPrincipal User user) {

        taskService.deleteTask(projectId, taskId, user);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully"));
    }
}