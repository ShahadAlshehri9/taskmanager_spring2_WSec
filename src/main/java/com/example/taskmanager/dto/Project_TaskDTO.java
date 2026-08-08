package com.example.taskmanager.dto;

import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.Status;

public record Project_TaskDTO(Long id, String title, UserTaskDTO leader) {
    public static Project_TaskDTO from(Project p) {
        return new Project_TaskDTO(
                p.getId(), p.getTitle(),
                p.getLeader() != null ? UserTaskDTO.from(p.getLeader()) : null);  // <-- UserDTO, not User
    }
}
