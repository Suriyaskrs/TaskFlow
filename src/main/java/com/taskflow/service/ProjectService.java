package com.taskflow.service;

import com.taskflow.dto.request.ProjectRequest;
import com.taskflow.dto.request.ProjectUpdateRequest;
import com.taskflow.dto.response.ProjectResponse;
import com.taskflow.dto.response.TaskResponse;
import com.taskflow.exception.BadRequestException;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.exception.UnauthorizedException;
import com.taskflow.model.Project;
import com.taskflow.model.TaskStatus;
import com.taskflow.model.User;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for project management.
 *
 * All methods accept the logged-in User object (resolved by controllers
 * from the security context) — no raw user IDs passed around.
 *
 * Ownership checks:
 *   - findByIdAndUser ensures project belongs to the requesting user.
 *   - Any access to another user's project returns 403.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    // -------------------------------------------------------
    // Create
    // -------------------------------------------------------

    /**
     * Creates a new project owned by the authenticated user.
     */
    @Transactional
    public ProjectResponse createProject(ProjectRequest request, User user) {
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .user(user)
                .build();

        Project saved = projectRepository.save(project);
        log.info("Project created: '{}' by user: {}", saved.getName(), user.getEmail());

        return buildProjectResponse(saved);
    }

    // -------------------------------------------------------
    // Read
    // -------------------------------------------------------

    /**
     * Returns all projects owned by the user (with progress stats).
     */
    @Transactional(readOnly = true)
    public List<ProjectResponse> getUserProjects(User user) {
        return projectRepository.findByUser(user).stream()
                .map(this::buildProjectResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns one project with its tasks list.
     * Throws 404 if project not found, 403 if not owned by user.
     */
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId, User user) {
        Project project = getProjectOwnedByUser(projectId, user);

        long total = taskRepository.countByProject(project);
        long completed = taskRepository.countByProjectAndStatus(project, TaskStatus.DONE);

        List<TaskResponse> tasks = taskRepository.findByProject(project)
                .stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());

        ProjectResponse response = ProjectResponse.from(project, total, completed);
        response.setTasks(tasks);
        return response;
    }

    // -------------------------------------------------------
    // Update
    // -------------------------------------------------------

    /**
     * Updates name and/or description of a project.
     * Partial update — only non-null fields are applied.
     * Throws BadRequestException if both fields are null (nothing to update).
     */
    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectUpdateRequest request, User user) {
        // Validate at least one field is provided
        if (request.getName() == null && request.getDescription() == null) {
            throw new BadRequestException("At least one field (name or description) must be provided to update.");
        }

        Project project = getProjectOwnedByUser(projectId, user);

        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }

        Project updated = projectRepository.save(project);
        log.info("Project updated: id={} by user: {}", projectId, user.getEmail());

        return buildProjectResponse(updated);
    }

    // -------------------------------------------------------
    // Delete
    // -------------------------------------------------------

    /**
     * Deletes a project and all its tasks (CascadeType.ALL handles task deletion).
     */
    @Transactional
    public void deleteProject(Long projectId, User user) {
        Project project = getProjectOwnedByUser(projectId, user);
        projectRepository.delete(project);
        log.info("Project deleted: id={} by user: {}", projectId, user.getEmail());
    }

    // -------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------

    /**
     * Loads a project and verifies it belongs to the given user.
     * Single point of ownership enforcement — reused across all methods.
     */
    public Project getProjectOwnedByUser(Long projectId, User user) {
        return projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(() -> {
                    boolean exists = projectRepository.existsById(projectId);
                    if (!exists) {
                        return new ResourceNotFoundException("Project", projectId);
                    }
                    return new UnauthorizedException("You do not have access to this project");
                });
    }

    private ProjectResponse buildProjectResponse(Project project) {
        long total = taskRepository.countByProject(project);
        long completed = taskRepository.countByProjectAndStatus(project, TaskStatus.DONE);
        return ProjectResponse.from(project, total, completed);
    }
}