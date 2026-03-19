package com.taskflow.service;

import com.taskflow.exception.BadRequestException;
import com.taskflow.model.User;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User account management.
 *
 * Delete account flow:
 *   1. Verify password (safety check — user must confirm)
 *   2. Delete all user's projects (cascade deletes their tasks)
 *   3. Delete the user record
 *
 * Password confirmation prevents accidental or unauthorized deletion.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Deletes the authenticated user's account.
     * Requires password confirmation for safety.
     * All projects and tasks belonging to the user are also deleted.
     */
    @Transactional
    public void deleteAccount(User user, String confirmPassword) {
        // Safety check: user must provide correct password to delete account
        if (!passwordEncoder.matches(confirmPassword, user.getPassword())) {
            throw new BadRequestException("Password is incorrect. Account deletion cancelled.");
        }

        // Delete all projects (CascadeType.ALL removes their tasks automatically)
        projectRepository.findByUser(user).forEach(projectRepository::delete);

        // Delete the user
        userRepository.delete(user);
        log.info("Account deleted for user: {}", user.getEmail());
    }
}