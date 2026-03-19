package com.taskflow.repository;

import com.taskflow.model.Priority;
import com.taskflow.model.Project;
import com.taskflow.model.Task;
import com.taskflow.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Task entity.
 *
 * Day 4 additions:
 *   - countByStatus (global, for admin stats)
 *   - countAllOverdueTasks (global, for admin stats)
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // -------------------------------------------------------
    // Basic lookups
    // -------------------------------------------------------

    List<Task> findByProject(Project project);

    Optional<Task> findByIdAndProject(Long id, Project project);

    // -------------------------------------------------------
    // Counts — project-scoped
    // -------------------------------------------------------

    long countByProject(Project project);

    long countByProjectAndStatus(Project project, TaskStatus status);

    long countByProjectAndPriority(Project project, Priority priority);

    // -------------------------------------------------------
    // Counts — global (admin)
    // -------------------------------------------------------

    /**
     * Count all tasks globally with a given status (used in admin stats).
     */
    long countByStatus(TaskStatus status);

    /**
     * Count all overdue tasks across ALL projects (admin stats).
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.deadline < :today AND t.status != 'DONE'")
    long countAllOverdueTasks(@Param("today") LocalDate today);

    // -------------------------------------------------------
    // Filtering
    // -------------------------------------------------------

    List<Task> findByProjectAndPriority(Project project, Priority priority);

    List<Task> findByProjectOrderByPriorityDesc(Project project);

    // -------------------------------------------------------
    // Pagination
    // -------------------------------------------------------

    Page<Task> findByProject(Project project, Pageable pageable);

    Page<Task> findByProjectAndStatus(Project project, TaskStatus status, Pageable pageable);

    // -------------------------------------------------------
    // Search
    // -------------------------------------------------------

    @Query("SELECT t FROM Task t WHERE t.project = :project " +
           "AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Task> searchByKeyword(
            @Param("project") Project project,
            @Param("keyword") String keyword,
            Pageable pageable);

    // -------------------------------------------------------
    // Overdue — project-scoped
    // -------------------------------------------------------

    @Query("SELECT t FROM Task t WHERE t.project = :project " +
           "AND t.deadline < :today AND t.status != 'DONE'")
    List<Task> findOverdueTasks(
            @Param("project") Project project,
            @Param("today") LocalDate today);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project = :project " +
           "AND t.deadline < :today AND t.status != 'DONE'")
    long countOverdueTasks(
            @Param("project") Project project,
            @Param("today") LocalDate today);

    // -------------------------------------------------------
    // Bulk operations
    // -------------------------------------------------------

    @Modifying
    @Query("DELETE FROM Task t WHERE t.project = :project AND t.status = :status")
    int deleteByProjectAndStatus(
            @Param("project") Project project,
            @Param("status") TaskStatus status);

    @Modifying
    @Query("DELETE FROM Task t WHERE t.project = :project")
    int deleteAllByProject(@Param("project") Project project);
}