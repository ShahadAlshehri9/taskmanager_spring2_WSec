package com.example.taskmanager.dto;

import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.Status;
import com.example.taskmanager.model.User;

public record UserTaskDTO(Long id, String username) {
    public static UserTaskDTO from(User u) {
        return new UserTaskDTO(u.getId(),u.getUsername());  // <-- UserDTO, not User
    }
    }