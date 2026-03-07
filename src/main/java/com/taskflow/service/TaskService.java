package com.taskflow.service;

import com.taskflow.dto.request.TaskRequest;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for task management.
 *
 * Business rules enforced here:
 *   1. DONE → TODO or IN_PROGRESS is not allowed (terminal state)
 *   2. Deadline must be in the future (also validated at DTO level)
 *   3. Tasks can only be accessed through their parent project
 *      (ProjectService.getProjectOwnedByUser ensures project ownership)
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

    /**
     * Creates a task inside the specified project.
     * Verifies the project belongs to the authenticated user first.
     */
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
    // Read
    // -------------------------------------------------------

    /**
     * Returns all tasks for a project (verifying ownership).
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByProject(Long projectId, User user) {
        Project project = projectService.getProjectOwnedByUser(projectId, user);
        return taskRepository.findByProject(project)
                .stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Returns tasks filtered by priority.
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByPriority(Long projectId, Priority priority, User user) {
        Project project = projectService.getProjectOwnedByUser(projectId, user);
        return taskRepository.findByProjectAndPriority(project, priority)
                .stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Returns overdue tasks (deadline passed, status != DONE).
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getOverdueTasks(Long projectId, User user) {
        Project project = projectService.getProjectOwnedByUser(projectId, user);
        return taskRepository.findOverdueTasks(project, LocalDate.now())
                .stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------
    // Update
    // -------------------------------------------------------

    /**
     * Updates a task's fields.
     *
     * Partial update strategy:
     *   - Only non-null fields in the request are applied.
     *   - Existing values are kept for null fields.
     *
     * Business rule: DONE is terminal — cannot be reverted.
     */
    @Transactional
    public TaskResponse updateTask(Long projectId, Long taskId, TaskRequest request, User user) {
        Project project = projectService.getProjectOwnedByUser(projectId, user);
        Task task = getTaskInProject(taskId, project);

        // Business rule: DONE status is terminal
        if (task.getStatus() == TaskStatus.DONE && request.getStatus() != null
                && request.getStatus() != TaskStatus.DONE) {
            throw new BadRequestException(
                    "Cannot revert task status from DONE. Task '" + task.getTitle() + "' is already completed."
            );
        }

        // Apply partial updates (only non-null fields)
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getDeadline() != null) {
            task.setDeadline(request.getDeadline());
        }

        Task updated = taskRepository.save(task);
        log.info("Task updated: id={} status={}", taskId, updated.getStatus());

        return TaskResponse.from(updated);
    }

    // -------------------------------------------------------
    // Delete
    // -------------------------------------------------------

    /**
     * Deletes a task (verifying project ownership).
     */
    @Transactional
    public void deleteTask(Long projectId, Long taskId, User user) {
        Project project = projectService.getProjectOwnedByUser(projectId, user);
        Task task = getTaskInProject(taskId, project);
        taskRepository.delete(task);
        log.info("Task deleted: id={} from project id={}", taskId, projectId);
    }

    // -------------------------------------------------------
    // Internal helper
    // -------------------------------------------------------

    private Task getTaskInProject(Long taskId, Project project) {
        return taskRepository.findByIdAndProject(taskId, project)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
    }
}