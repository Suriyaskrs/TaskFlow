package com.taskflow.service;

import com.taskflow.dto.request.TaskRequest;
import com.taskflow.dto.response.PagedResponse;
import com.taskflow.dto.response.TaskResponse;
import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.model.Priority;
import com.taskflow.model.Project;
import com.taskflow.model.Task;
import com.taskflow.model.TaskStatus;
import com.taskflow.model.User;
import com.taskflow.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for task management.
 *
 * Day 3 additions:
 *   - Paginated task listing with sorting
 *   - Keyword search
 *   - Bulk delete by status
 *   - Delete all tasks in a project
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectService projectService;

    // -------------------------------------------------------
    // Create
    // -------------------------------------------------------

    @Transactional
    public TaskResponse createTask(Long projectId, TaskRequest request, User user) {
        Project project = projectService.getProjectOwnedByUser(projectId, user);

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO)
                .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
                .deadline(request.getDeadline())
                .project(project)
                .build();

        Task saved = taskRepository.save(task);
        log.info("Task created: '{}' in project id={}", saved.getTitle(), projectId);
        return TaskResponse.from(saved);
    }

    // -------------------------------------------------------
    // Read — basic
    // -------------------------------------------------------

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByProject(Long projectId, User user) {
        Project project = projectService.getProjectOwnedByUser(projectId, user);
        return taskRepository.findByProject(project)
                .stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByPriority(Long projectId, Priority priority, User user) {
        Project project = projectService.getProjectOwnedByUser(projectId, user);
        return taskRepository.findByProjectAndPriority(project, priority)
                .stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getOverdueTasks(Long projectId, User user) {
        Project project = projectService.getProjectOwnedByUser(projectId, user);
        return taskRepository.findOverdueTasks(project, LocalDate.now())
                .stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------
    // Read — paginated (Day 3)
    // -------------------------------------------------------

    /**
     * Paginated task listing with optional status filter and sorting.
     *
     * @param page   page number (0-based)
     * @param size   items per page (default 10, max 50)
     * @param sort   field to sort by: "priority", "deadline", "createdAt", "status"
     * @param status optional filter — null means all statuses
     */
    @Transactional(readOnly = true)
    public PagedResponse<TaskResponse> getTasksPaged(
            Long projectId, int page, int size, String sort,
            TaskStatus status, User user) {

        // Cap page size to prevent abuse
        size = Math.min(size, 50);

        Project project = projectService.getProjectOwnedByUser(projectId, user);

        // Build sort — default to createdAt desc if invalid field given
        Sort sortOrder = buildSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Page<Task> taskPage;
        if (status != null) {
            taskPage = taskRepository.findByProjectAndStatus(project, status, pageable);
        } else {
            taskPage = taskRepository.findByProject(project, pageable);
        }

        Page<TaskResponse> responsePage = taskPage.map(TaskResponse::from);
        return PagedResponse.from(responsePage);
    }

    /**
     * Paginated keyword search across task title and description.
     */
    @Transactional(readOnly = true)
    public PagedResponse<TaskResponse> searchTasks(
            Long projectId, String keyword, int page, int size, User user) {

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new BadRequestException("Search keyword cannot be empty");
        }

        size = Math.min(size, 50);
        Project project = projectService.getProjectOwnedByUser(projectId, user);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Task> taskPage = taskRepository.searchByKeyword(project, keyword.trim(), pageable);

        Page<TaskResponse> responsePage = taskPage.map(TaskResponse::from);
        return PagedResponse.from(responsePage);
    }

    // -------------------------------------------------------
    // Update
    // -------------------------------------------------------

    @Transactional
    public TaskResponse updateTask(Long projectId, Long taskId, TaskRequest request, User user) {
        Project project = projectService.getProjectOwnedByUser(projectId, user);
        Task task = getTaskInProject(taskId, project);

        // Business rule: DONE is terminal
        if (task.getStatus() == TaskStatus.DONE && request.getStatus() != null
                && request.getStatus() != TaskStatus.DONE) {
            throw new BadRequestException(
                    "Cannot revert task from DONE. Task '" + task.getTitle() + "' is already completed."
            );
        }

        if (request.getTitle() != null)       task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getStatus() != null)      task.setStatus(request.getStatus());
        if (request.getPriority() != null)    task.setPriority(request.getPriority());
        if (request.getDeadline() != null)    task.setDeadline(request.getDeadline());

        Task updated = taskRepository.save(task);
        log.info("Task updated: id={} status={}", taskId, updated.getStatus());
        return TaskResponse.from(updated);
    }

    // -------------------------------------------------------
    // Delete — single (Day 1)
    // -------------------------------------------------------

    @Transactional
    public void deleteTask(Long projectId, Long taskId, User user) {
        Project project = projectService.getProjectOwnedByUser(projectId, user);
        Task task = getTaskInProject(taskId, project);
        taskRepository.delete(task);
        log.info("Task deleted: id={} from project id={}", taskId, projectId);
    }

    // -------------------------------------------------------
    // Delete — bulk (Day 3)
    // -------------------------------------------------------

    /**
     * Bulk delete all tasks with a given status in a project.
     * Example: clear all DONE tasks after a sprint.
     * Returns count of deleted tasks.
     */
    @Transactional
    public int deleteTasksByStatus(Long projectId, TaskStatus status, User user) {
        Project project = projectService.getProjectOwnedByUser(projectId, user);
        int deleted = taskRepository.deleteByProjectAndStatus(project, status);
        log.info("Bulk deleted {} tasks with status={} from project id={}", deleted, status, projectId);
        return deleted;
    }

    /**
     * Delete ALL tasks in a project without deleting the project itself.
     * Useful for resetting a project.
     * Returns count of deleted tasks.
     */
    @Transactional
    public int deleteAllTasks(Long projectId, User user) {
        Project project = projectService.getProjectOwnedByUser(projectId, user);
        int deleted = taskRepository.deleteAllByProject(project);
        log.info("Deleted all {} tasks from project id={}", deleted, projectId);
        return deleted;
    }

    // -------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------

    private Task getTaskInProject(Long taskId, Project project) {
        return taskRepository.findByIdAndProject(taskId, project)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
    }

    private Sort buildSort(String sort) {
        if (sort == null) return Sort.by(Sort.Direction.DESC, "createdAt");

        return switch (sort.toLowerCase()) {
            case "priority"  -> Sort.by(Sort.Direction.DESC, "priority");
            case "deadline"  -> Sort.by(Sort.Direction.ASC, "deadline");
            case "status"    -> Sort.by(Sort.Direction.ASC, "status");
            case "title"     -> Sort.by(Sort.Direction.ASC, "title");
            default          -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}