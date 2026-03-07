package com.taskflow.exception;

/**
 * Thrown when a requested resource (project, task, user) does not exist.
 * Maps to HTTP 404 Not Found in GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with id: " + id);
    }
}