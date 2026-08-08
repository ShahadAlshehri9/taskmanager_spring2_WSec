package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.exception.ProjectNotFoundException;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.exception.ValidationException;
import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.Status;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.ProjectRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectService {
private final ProjectRepository repository;
private final UserRepository userRepository;
private final TaskRepository taskRepository;

    public ProjectService(ProjectRepository repository,
                          UserRepository userRepository,
                          TaskRepository taskRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }
private User currentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user named " + username));
    }
    public Project addProject(Project project, String username) {
        if (project.getTitle() == null || project.getTitle().isBlank()) {
            throw new ValidationException("Project title must not be empty");
        }
        project.setLeader(currentUser(username));   // stamp the leader before saving
        return repository.save(project);
    }

    public Project getById(Long id, String username) {
        return repository.findByIdAndLeader(id, currentUser(username))
                .orElseThrow(() -> new ProjectNotFoundException(id));
    }
    public List<Project> getAll(String username) {
        return repository.findByLeader(currentUser(username));
    }

    //for the admin -
    public List<Project> getAllProjects() {
        return repository.findAll();
    }
    public Project changeStatus(Long id, Status status, String username) {
        Project project = repository.findByIdAndLeader(id,currentUser(username))
                .orElseThrow(() -> new UsernameNotFoundException("project not found"));
        project.setStatus(status);
        return repository.save(project);
    }
    public Project update(Long id, Project data, String username) {
        Project existing = getById(id, username); // throws ProjectNotFoundException (-> 404) if missing or not owned
        if (data.getTitle() == null || data.getTitle().isBlank()) {
            throw new ValidationException("Project title must not be empty");
        }
        existing.setTitle(data.getTitle());
        existing.setDescription(data.getDescription());
        if (data.getStatus() != null) {
            existing.setStatus(data.getStatus());
        }
        existing.setDueDate(data.getDueDate());
        return repository.save(existing);
    }

    public void delete(Long id, String username) {
        // only delete if that id exists AND belongs to this user
        if (!repository.existsByIdAndLeader(id, currentUser(username))) {
            throw new ProjectNotFoundException(id);
        }
        repository.deleteById(id);
    }


    public List<Project> search(String keyword, String username) {
        String key = keyword.toLowerCase();
        return repository.findByLeader(currentUser(username)).stream()
                .filter(t -> t.getTitle().toLowerCase().contains(key)//avoid case sensitivity
                        || (t.getDescription() != null && t.getDescription().toLowerCase().contains(key)))
                .toList();
    }
    public List<Project> searchAdmin(String keyword) {
        String key = keyword.toLowerCase();
        return repository.findAll().stream()
                .filter(t -> t.getTitle().toLowerCase().contains(key)//avoid case sensitivity
                        || (t.getDescription() != null && t.getDescription().toLowerCase().contains(key)))
                .toList();
    }

    public List<Project> overdue(LocalDate today, String username) {
        return repository.findByLeader(currentUser(username)).stream()
                .filter(t -> t.isOverdue(today))
                .sorted(Comparator.comparing(Project::getDueDate))
                .toList();
    }



    public Map<Status, Long> countByStatus(String username) {
        return repository.findByLeader(currentUser(username)).stream()
                .collect(Collectors.groupingBy(Project::getStatus, Collectors.counting()));//creates a new map object that holds the collected values.
    }

    public List<TaskDTO> getAllTasks(String username, String projectTitle) {

        Optional<Project> project = repository
                .findByLeader(currentUser(username))
                .stream()
                .filter(p -> p.getTitle().equalsIgnoreCase(projectTitle))
                .findFirst();

        if (project.isEmpty()) {
            return Collections.emptyList();
        }

        Project selectedProject = project.get();

        return taskRepository.findAll()
                .stream()
                .filter(task -> task.getProject() != null)
                .filter(task -> task.getProject().getId().equals(selectedProject.getId()))
                .map(TaskDTO::from).toList();
    }


    public void deleteProject(Long id,String username){
        if (!repository.existsByIdAndLeader(id, currentUser(username))) {
            throw new ProjectNotFoundException(id);
        }
        repository.deleteById(id);
    }

    public Project addTeamMember(Long projectId, String targetUsername, String leaderUsername) {
        // this method already ensures the caller is the Leader
        Project project = getById(projectId, leaderUsername);

        // find the target user by exact match
        User newMember = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // add to team and save
        project.getTeamMembers().add(newMember);
        return repository.save(project);
    }

    public Project removeTeamMember(Long projectId, String targetUsername, String leaderUsername) {
        Project project = getById(projectId, leaderUsername);

        User memberToRemove = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        project.getTeamMembers().remove(memberToRemove);
        return repository.save(project);
    }




}
