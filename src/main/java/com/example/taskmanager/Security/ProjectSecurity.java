package com.example.taskmanager.Security;

import com.example.taskmanager.repository.ProjectRepository;
import org.springframework.stereotype.Component;

@Component("projectSecurity")
public class ProjectSecurity {

    private final ProjectRepository projects;

    public ProjectSecurity(ProjectRepository projects) { this.projects = projects; }

    // true only if is the leader of project
    public boolean isLeader(Long projectId, String username) {
        return projects.findById(projectId)
                .map(p -> p.getLeader() != null
                        && p.getLeader().getUsername().equals(username))
                .orElse(false);
    }
}