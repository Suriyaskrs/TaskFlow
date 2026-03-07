package com.taskflow.repository;

import com.taskflow.model.Project;
import com.taskflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Project entity.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Get all projects owned by a specific user.
     * Used in GET /projects — returns only the logged-in user's projects.
     */
    List<Project> findByUser(User user);

    /**
     * Find a project by id AND user.
     * Prevents users from accessing or modifying another user's project.
     */
    Optional<Project> findByIdAndUser(Long id, User user);

    /**
     * Count projects belonging to a user.
     */
    long countByUser(User user);
}