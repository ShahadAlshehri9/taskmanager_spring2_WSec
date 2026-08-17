package com.example.taskmanager.Security;

import com.example.taskmanager.model.Role;

import com.example.taskmanager.repository.ProjectRepository;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("projectSecurity")
public class ProjectSecurity {

    private final ProjectRepository projects;
    private final UserRepository users;

    public ProjectSecurity(ProjectRepository projects,UserRepository user) { this.projects = projects;this.users=user; }

    // true only if is the leader of project
    public boolean isLeader(Long projectId, String username) {
        return projects.findById(projectId)
                .map(p -> p.getLeader() != null
                        && p.getLeader().getUsername().equals(username))
                .orElse(false);
    }
    public boolean isManager(String username) {
        return users.findByUsername(username)
                .map(user -> user.getRole() == Role.MANAGER)
                .orElse(false);
    }

    // true only if the user is a team member of the project
    public boolean isMember(Long projectId, String username) {
        return projects.findByIdAndTeamMembers_Username(projectId, username).isPresent();
    }
}