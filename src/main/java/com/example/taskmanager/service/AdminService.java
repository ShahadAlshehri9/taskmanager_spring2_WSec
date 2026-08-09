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

    public AdminService(UserRepository userRepository,
                        TaskRepository taskRepository,
                        ProjectRepository projectRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    /** Every account as a safe DTO (no password). */
    public List<UserDTO> listUsers() {
        return userRepository.findAll().stream()
                .map(UserDTO::from)
                .toList();
    }

    /**
     * Delete an account and everything that references it, in FK-safe order:
     * team memberships, then owned tasks, then led projects, then the account.
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ValidationException("No user with id " + id));
        if (user.getRole() == Role.ADMIN) {
            throw new ValidationException("Admin accounts cannot be deleted");
        }

        //remove them from every team (clears project_team rows)
        List<Project> teams = projectRepository.findByTeamMembersContains(user);
        for (Project p : teams) {
            p.getTeamMembers().remove(user);
        }
        projectRepository.saveAll(teams);

        //detach them as leader of any project they lead (keep the project)
        List<Project> led = projectRepository.findByLeader(user);
        for (Project p : led) {
            p.setLeader(null);
        }
        projectRepository.saveAll(led);

        // their owned tasks, then  the account
        taskRepository.deleteByOwner(user);
        userRepository.delete(user);
    }

    @Transactional
    public UserDTO changeRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ValidationException("No user with id " + id));
        user.setRole(role);
        return UserDTO.from(userRepository.save(user));
    }

}