package com.example.taskmanager.exception;

/** Thrown when a task id does not exist. */
public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(Long id) {
        super("No task found with id " + id);
    }
}
