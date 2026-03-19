package com.taskflow.service;

import com.taskflow.dto.response.AnalyticsResponse;
import com.taskflow.model.Priority;
import com.taskflow.model.Project;
import com.taskflow.model.TaskStatus;
import com.taskflow.model.User;
import com.taskflow.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Computes analytics metrics for a project.
 *
 * All counts are done at the database level (COUNT queries)
 * for efficiency — never loads all tasks into memory.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final TaskRepository taskRepository;
    private final ProjectService projectService;

    /**
     * Returns full analytics for a single project.
     * Verifies ownership before computing.
     */
    @Transactional(readOnly = true)
    public AnalyticsResponse getProjectAnalytics(Long projectId, User user) {
        Project project = projectService.getProjectOwnedByUser(projectId, user);

        // Status counts
        long total       = taskRepository.countByProject(project);
        long todo        = taskRepository.countByProjectAndStatus(project, TaskStatus.TODO);
        long inProgress  = taskRepository.countByProjectAndStatus(project, TaskStatus.IN_PROGRESS);
        long done        = taskRepository.countByProjectAndStatus(project, TaskStatus.DONE);

        // Priority counts
        long high        = taskRepository.countByProjectAndPriority(project, Priority.HIGH);
        long medium      = taskRepository.countByProjectAndPriority(project, Priority.MEDIUM);
        long low         = taskRepository.countByProjectAndPriority(project, Priority.LOW);

        // Overdue count
        long overdue     = taskRepository.countOverdueTasks(project, LocalDate.now());

        // Completion rate
        double completionRate = total == 0 ? 0.0
                : Math.round((done * 100.0 / total) * 10.0) / 10.0;

        return AnalyticsResponse.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .totalTasks(total)
                .todoTasks(todo)
                .inProgressTasks(inProgress)
                .doneTasks(done)
                .highPriorityTasks(high)
                .mediumPriorityTasks(medium)
                .lowPriorityTasks(low)
                .overdueTasks(overdue)
                .completionRate(completionRate)
                .build();
    }
}