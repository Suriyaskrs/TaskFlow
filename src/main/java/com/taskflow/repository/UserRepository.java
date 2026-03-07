package com.taskflow.repository;

import com.taskflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity.
 * Spring Data JPA auto-implements all CRUD operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by email (used for login authentication).
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if email is already registered (used during registration).
     */
    boolean existsByEmail(String email);
}