package com.taskflow.service;

import com.taskflow.dto.response.AdminStatsResponse;
import com.taskflow.dto.response.PagedResponse;
import com.taskflow.dto.response.ProjectResponse;
import com.taskflow.dto.response.UserResponse;
import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.model.Task;
import com.taskflow.model.TaskStatus;
import com.taskflow.model.User;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Admin service — unrestricted access across all users' data.
 *
 * Security note:
 *   These methods trust that the caller IS an admin.
 *   The role check is enforced at two levels:
 *     1. SecurityConfig → /admin/** requires ROLE_ADMIN
 *     2. @PreAuthorize("hasRole('ADMIN')") on the controller
 *   So no extra user check is needed inside this service.
 *
 * Admin capabilities:
 *   - View all users (paginated)
 *   - Delete any user by ID (cascades to their projects + tasks)
 *   - View all projects (paginated)
 *   - Delete any project by ID (cascades to its tasks)
 *   - Delete any task by ID
 *   - System-wide stats
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    // -------------------------------------------------------
    // User management
    // -------------------------------------------------------

    /**
     * Lists all users with their project counts (paginated).
     */
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getAllUsers(int page, int size) {
        size = Math.min(size, 100);
        Page<User> users = userRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id")));

        Page<UserResponse> response = users.map(user -> {
            long projectCount = projectRepository.countByUser(user);
            return UserResponse.from(user, projectCount);
        });

        return PagedResponse.from(response);
    }

    /**
     * Deletes any user by ID regardless of who they are.
     * Also deletes all their projects and tasks (via cascade).
     * Cannot delete yourself as admin — safety guard.
     */
    @Transactional
    public void deleteUserById(Long userId, User adminUser) {
        // Safety: admin cannot delete their own account via this endpoint
        if (adminUser.getId().equals(userId)) {
            throw new BadRequestException(
                    "Cannot delete your own admin account via this endpoint. Use /users/me instead.");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Delete all projects (CascadeType.ALL removes their tasks)
        projectRepository.findByUser(targetUser).forEach(projectRepository::delete);

        userRepository.delete(targetUser);
        log.info("ADMIN deleted user id={} email={}", userId, targetUser.getEmail());
    }

    // -------------------------------------------------------
    // Project management
    // -------------------------------------------------------

    /**
     * Lists all projects across all users (paginated).
     * Includes progress stats per project.
     */
    @Transactional(readOnly = true)
    public PagedResponse<ProjectResponse> getAllProjects(int page, int size) {
        size = Math.min(size, 100);
        Page<com.taskflow.model.Project> projects = projectRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        Page<ProjectResponse> response = projects.map(project -> {
            long total = taskRepository.countByProject(project);
            long completed = taskRepository.countByProjectAndStatus(project, TaskStatus.DONE);
            return ProjectResponse.from(project, total, completed);
        });

        return PagedResponse.from(response);
    }

    /**
     * Deletes any project by ID (and all its tasks).
     */
    @Transactional
    public void deleteProjectById(Long projectId) {
        com.taskflow.model.Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        projectRepository.delete(project);
        log.info("ADMIN deleted project id={} name='{}'", projectId, project.getName());
    }

    // -------------------------------------------------------
    // Task management
    // -------------------------------------------------------

    /**
     * Deletes any task by ID regardless of which project it belongs to.
     */
    @Transactional
    public void deleteTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        taskRepository.delete(task);
        log.info("ADMIN deleted task id={} title='{}'", taskId, task.getTitle());
    }

    // -------------------------------------------------------
    // System-wide stats
    // -------------------------------------------------------

    /**
     * Returns system-wide aggregate stats.
     * Uses COUNT queries — never loads full collections into memory.
     */
    @Transactional(readOnly = true)
    public AdminStatsResponse getSystemStats() {
        long totalUsers    = userRepository.count();
        long totalProjects = projectRepository.count();
        long totalTasks    = taskRepository.count();
        long completedTasks = taskRepository.countByStatus(TaskStatus.DONE);
        long overdueTasks  = taskRepository.countAllOverdueTasks(LocalDate.now());

        double completionRate = totalTasks == 0 ? 0.0
                : Math.round((completedTasks * 100.0 / totalTasks) * 10.0) / 10.0;

        return AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalProjects(totalProjects)
                .totalTasks(totalTasks)
                .totalCompletedTasks(completedTasks)
                .totalOverdueTasks(overdueTasks)
                .overallCompletionRate(completionRate)
                .build();
    }
}