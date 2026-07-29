package com.example.taskmanager.exception;

/** Thrown when a task fails a  rule (e.g. blank title). */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
