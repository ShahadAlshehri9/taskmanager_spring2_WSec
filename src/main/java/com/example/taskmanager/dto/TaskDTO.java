package com.example.taskmanager.dto;

import com.example.taskmanager.model.Priority;
import com.example.taskmanager.model.Status;
import com.example.taskmanager.model.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskDTO(
        Long id, String title, String description,
        Priority priority, Status status, LocalDate dueDate, LocalDateTime createdAt,
        UserTaskDTO owner, Project_TaskDTO project) {

    public static TaskDTO from(Task t) {
        return new TaskDTO(
                t.getId(), t.getTitle(), t.getDescription(),
                t.getPriority(), t.getStatus(), t.getDueDate(), t.getCreatedAt(),
                t.getOwner()   != null ? UserTaskDTO.from(t.getOwner())        : null,
                t.getProject() != null ? Project_TaskDTO.from(t.getProject()) : null);
    }
}

