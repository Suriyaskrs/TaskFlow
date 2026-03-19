package com.taskflow.repository;

import com.taskflow.model.Project;
import com.taskflow.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Project entity.
 *
 * Day 4 additions:
 *   - findAll(Pageable) for admin paginated project listing
 *   - countByUser for admin user stats
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // User-scoped queries
    List<Project> findByUser(User user);

    Optional<Project> findByIdAndUser(Long id, User user);

    long countByUser(User user);

    // Admin queries — no user scope restriction
    Page<Project> findAll(Pageable pageable);
}