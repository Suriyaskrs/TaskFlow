package com.taskflow.model;

/**
 * Defines valid task lifecycle statuses.
 *
 * Allowed transitions:
 *   TODO → IN_PROGRESS
 *   IN_PROGRESS → DONE
 *   IN_PROGRESS → TODO  (allow un-starting a task)
 *
 * Business rule enforced in TaskService:
 *   DONE is terminal — cannot revert to TODO or IN_PROGRESS.
 */
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE
}