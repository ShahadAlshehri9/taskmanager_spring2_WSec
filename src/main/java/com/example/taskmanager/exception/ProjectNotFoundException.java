package com.example.taskmanager.exception;

/** Thrown when a project id does not exist. */
public class ProjectNotFoundException extends RuntimeException {
    public ProjectNotFoundException(Long id) {
        super("No Project found with id " + id);
    }
}
