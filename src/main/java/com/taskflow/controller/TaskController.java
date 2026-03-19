package com.taskflow.controller;

import com.taskflow.dto.request.TaskRequest;
import com.taskflow.dto.response.ApiResponse;
import com.taskflow.dto.response.PagedResponse;
import com.taskflow.dto.response.TaskResponse;
import com.taskflow.model.Priority;
import com.taskflow.model.TaskStatus;
import com.taskflow.model.User;
import com.taskflow.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
 * All tasks are scoped under /projects/{projectId}/tasks
 * to enforce ownership and clear URL hierarchy.
 *
 * Day 3 additions:
 *   - GET /tasks?page=0&size=10&sort=priority&status=TODO  (paginated)
 *   - GET /tasks/search?keyword=login&page=0&size=10
 *   - DELETE /tasks/status/{status}  (bulk delete by status)
 *   - DELETE /tasks/all              (delete all tasks in project)
 */
@RestController
@RequestMapping("/projects/{projectId}/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task CRUD, search, and bulk operations")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    // -------------------------------------------------------
    // Create
    // -------------------------------------------------------

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

    // -------------------------------------------------------
    // Read — simple list (backward compatible)
    // -------------------------------------------------------

    @GetMapping("/all")
    @Operation(summary = "Get all tasks in a project (no pagination)")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getAllTasks(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User user) {

        List<TaskResponse> tasks = taskService.getTasksByProject(projectId, user);
        return ResponseEntity.ok(ApiResponse.success("Tasks retrieved", tasks));
    }

    // -------------------------------------------------------
    // Read — paginated (Day 3)
    // -------------------------------------------------------

    /**
     * GET /projects/{projectId}/tasks?page=0&size=10&sort=priority&status=TODO
     *
     * sort options: priority, deadline, createdAt, status, title
     * status filter: TODO, IN_PROGRESS, DONE (optional — omit for all)
     */
    @GetMapping
    @Operation(summary = "Get tasks with pagination, sorting, and optional status filter")
    public ResponseEntity<ApiResponse<PagedResponse<TaskResponse>>> getTasksPaged(
            @PathVariable Long projectId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page (max 50)") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by: priority, deadline, createdAt, status, title")
                @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "Filter by status: TODO, IN_PROGRESS, DONE")
                @RequestParam(required = false) TaskStatus status,
            @AuthenticationPrincipal User user) {

        PagedResponse<TaskResponse> tasks = taskService.getTasksPaged(
                projectId, page, size, sort, status, user);
        return ResponseEntity.ok(ApiResponse.success("Tasks retrieved", tasks));
    }

    // -------------------------------------------------------
    // Read — search (Day 3)
    // -------------------------------------------------------

    /**
     * GET /projects/{projectId}/tasks/search?keyword=login&page=0&size=10
     * Searches title and description (case-insensitive).
     */
    @GetMapping("/search")
    @Operation(summary = "Search tasks by keyword in title or description")
    public ResponseEntity<ApiResponse<PagedResponse<TaskResponse>>> searchTasks(
            @PathVariable Long projectId,
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user) {

        PagedResponse<TaskResponse> tasks = taskService.searchTasks(
                projectId, keyword, page, size, user);
        return ResponseEntity.ok(ApiResponse.success("Search results", tasks));
    }

    // -------------------------------------------------------
    // Read — filters
    // -------------------------------------------------------

    @GetMapping("/priority/{priority}")
    @Operation(summary = "Get tasks by priority (LOW, MEDIUM, HIGH)")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByPriority(
            @PathVariable Long projectId,
            @PathVariable Priority priority,
            @AuthenticationPrincipal User user) {

        List<TaskResponse> tasks = taskService.getTasksByPriority(projectId, priority, user);
        return ResponseEntity.ok(ApiResponse.success("Tasks retrieved by priority", tasks));
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get overdue tasks (deadline passed, not DONE)")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getOverdueTasks(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User user) {

        List<TaskResponse> tasks = taskService.getOverdueTasks(projectId, user);
        return ResponseEntity.ok(ApiResponse.success("Overdue tasks retrieved", tasks));
    }

    // -------------------------------------------------------
    // Update
    // -------------------------------------------------------

    @PutMapping("/{taskId}")
    @Operation(summary = "Update a task (partial update — send only changed fields)")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestBody TaskRequest request,
            @AuthenticationPrincipal User user) {

        TaskResponse task = taskService.updateTask(projectId, taskId, request, user);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", task));
    }

    // -------------------------------------------------------
    // Delete — single
    // -------------------------------------------------------

    @DeleteMapping("/{taskId}")
    @Operation(summary = "Delete a single task")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @AuthenticationPrincipal User user) {

        taskService.deleteTask(projectId, taskId, user);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully"));
    }

    // -------------------------------------------------------
    // Delete — bulk (Day 3)
    // -------------------------------------------------------

    /**
     * DELETE /projects/{projectId}/tasks/status/{status}
     * Deletes all tasks with the given status.
     * Example: clear all DONE tasks after completing a sprint.
     */
    @DeleteMapping("/status/{status}")
    @Operation(summary = "Bulk delete all tasks with a given status")
    public ResponseEntity<ApiResponse<String>> deleteTasksByStatus(
            @PathVariable Long projectId,
            @PathVariable TaskStatus status,
            @AuthenticationPrincipal User user) {

        int deleted = taskService.deleteTasksByStatus(projectId, status, user);
        return ResponseEntity.ok(
                ApiResponse.success(deleted + " task(s) with status " + status + " deleted", null));
    }

    /**
     * DELETE /projects/{projectId}/tasks/all
     * Deletes ALL tasks in a project (resets the project without deleting it).
     */
    @DeleteMapping("/all")
    @Operation(summary = "Delete all tasks in a project (project itself is kept)")
    public ResponseEntity<ApiResponse<String>> deleteAllTasks(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User user) {

        int deleted = taskService.deleteAllTasks(projectId, user);
        return ResponseEntity.ok(
                ApiResponse.success("All " + deleted + " task(s) deleted from project", null));
    }
}