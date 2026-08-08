package com.example.taskmanager.dto;

import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.Status;
import com.example.taskmanager.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ProjectDTO(
        Long id, String title, String description,
        Status status, LocalDate dueDate, LocalDateTime createdAt,
        String leader, List<String> teamMembers) {

    public static ProjectDTO from(Project p) {
        return new ProjectDTO(
                p.getId(), p.getTitle(), p.getDescription(),
                p.getStatus(), p.getDueDate(), p.getCreatedAt(),
                p.getLeader() != null ? p.getLeader().getUsername() : null,
                p.getTeamMembers().stream().map(User::getUsername).toList());
    }
}