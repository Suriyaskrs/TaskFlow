package com.taskflow.exception;

/**
 * Thrown when business logic validation fails.
 * Example: trying to revert a DONE task, or duplicate email registration.
 * Maps to HTTP 400 Bad Request in GlobalExceptionHandler.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}