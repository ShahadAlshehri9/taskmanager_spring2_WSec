package com.example.taskmanager.service;
import com.example.taskmanager.dto.UserDTO;
import com.example.taskmanager.exception.ProjectNotFoundException;
import com.example.taskmanager.exception.ValidationException;
import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.Role;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.ProjectRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Admin business logic: list and delete accounts. It contains NO task-reading
// logic - TaskRepository is here only to clean up a deleted user's tasks.
@Service
public class AdminService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public AdminService(UserRepository userRepository, TaskRepository taskRepository, ProjectRepository projectRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    // Every account, mapped to the safe UserView (no password).
    public List<UserDTO> listUsers() {
        return userRepository.findAll().stream() // Fetches heavy User entities from DB
                .map(UserDTO::from) // Safely converts User to UserView dto where no password can be seen by the admin.
                .toList(); // a list with no passwords
    }

    // One transaction: delete the user's tasks, then the user - or roll BOTH
    // back on any failure, so we never end up half-deleted.
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ValidationException("No user with id " + id));
        if (user.getRole() == Role.ADMIN) {
            throw new ValidationException("Admin accounts cannot be deleted");
        }
        List<Project> teams = projectRepository.findByTeamMembersContains(user);
        for (Project p : teams) {
            p.getTeamMembers().remove(user);
        }
        projectRepository.saveAll(teams);
        taskRepository.deleteByOwner(user);  // tasks first (owner_id foreign key)
        projectRepository.deleteByLeader(user);
        userRepository.delete(user);        // then the account

    }
    public Project assignLeader(Long projectId, String username) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
        User leader = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user named " + username));
        project.setLeader(leader);
        project.getTeamMembers().add(leader);
        return projectRepository.save(project);
    }


    public Project revokeLeader(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
        project.setLeader(null);
        return projectRepository.save(project);
    }

}