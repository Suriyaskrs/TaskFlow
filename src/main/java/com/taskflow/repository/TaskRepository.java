package com.taskflow.repository;

import com.taskflow.model.Priority;
import com.taskflow.model.Project;
import com.taskflow.model.Task;
import com.taskflow.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Task entity.
 * Includes filtering by status, priority, and deadline — supports future advanced features.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Get all tasks for a specific project.
     */
    List<Task> findByProject(Project project);

    /**
     * Find a task by id that belongs to a specific project.
     * Prevents cross-project task access.
     */
    Optional<Task> findByIdAndProject(Long id, Project project);

    /**
     * Count tasks in a project (for progress calculation).
     */
    long countByProject(Project project);

    /**
     * Count completed (DONE) tasks in a project.
     */
    long countByProjectAndStatus(Project project, TaskStatus status);

    /**
     * Filter tasks by status within a project (pageable for future use).
     */
    Page<Task> findByProjectAndStatus(Project project, TaskStatus status, Pageable pageable);

    /**
     * Filter tasks by priority within a project.
     */
    List<Task> findByProjectAndPriority(Project project, Priority priority);

    /**
     * Find overdue tasks — deadline before today and not yet DONE.
     */
    @Query("SELECT t FROM Task t WHERE t.project = :project AND t.deadline < :today AND t.status != 'DONE'")
    List<Task> findOverdueTasks(@Param("project") Project project, @Param("today") LocalDate today);

    /**
     * Find tasks for a project sorted by priority descending.
     * Used in advanced task listing.
     */
    List<Task> findByProjectOrderByPriorityDesc(Project project);
}