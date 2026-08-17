package com.example.taskmanager.service;

import com.example.taskmanager.exception.ProjectNotFoundException;
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

import java.util.List;
import java.util.Optional;
@Service
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;


    public DashboardService( ProjectRepository projectRepository, UserRepository userRepository,TaskRepository taskRepository){
        this.projectRepository=projectRepository;
        this.taskRepository=taskRepository;
        this.userRepository=userRepository;

    }

    private int progressPercent(List<Task> tasks) {
        if (tasks.isEmpty()) return 0;
        long done = tasks.stream()
                .filter(t -> t.getStatus() == Status.DONE)
                .count();
        return (int) Math.round(done * 100.0 / tasks.size());
    }
    public User currentUser(String username){
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("No user named " + username));
    }
    public int projectProgress(Long projectId, String requester) {
        User u = currentUser(requester);
        Optional<Project> projectOpt = projectRepository.findByIdAndLeader(projectId, u);
        if (projectOpt.isEmpty()) {
            projectOpt = projectRepository.findByIdAndTeamMembers_Username(projectId, u.getUsername());
        }
        Project project = projectOpt.orElseThrow(() ->
                new ValidationException("Project not found or user does not have access."));

        List<Task> tasks = taskRepository.findByProject(project);
        return progressPercent(tasks);
    }
    public int projectProgressA(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ValidationException("Project not found."));
        List<Task> tasks = taskRepository.findByProject(project);
        return progressPercent(tasks);
    }
    public int myProgress(String username) {
        return progressPercent(taskRepository.findByOwner(currentUser(username)));
    }

}
