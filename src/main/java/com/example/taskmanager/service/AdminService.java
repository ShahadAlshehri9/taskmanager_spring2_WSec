package com.example.taskmanager.service;
import com.example.taskmanager.dto.UserDTO;
import com.example.taskmanager.exception.ValidationException;
import com.example.taskmanager.model.Role;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.ProjectRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
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
        taskRepository.deleteByOwner(user);  // tasks first (owner_id foreign key)
        projectRepository.deleteByLeader(user);
        userRepository.delete(user);        // then the account

    }

}