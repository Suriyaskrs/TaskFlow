package com.taskflow.exception;

/**
 * Thrown when a user tries to access a resource they don't own.
 * Maps to HTTP 403 Forbidden in GlobalExceptionHandler.
 *
 * Distinct from 401 Unauthorized (which is unauthenticated access —
 * that is handled automatically by Spring Security).
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}