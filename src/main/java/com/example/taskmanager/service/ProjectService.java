package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.dto.UserDTO;
import com.example.taskmanager.exception.ProjectNotFoundException;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.exception.ValidationException;
import com.example.taskmanager.model.*;
import com.example.taskmanager.repository.ProjectRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectService {
    private final ProjectRepository repository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ActivityService activityService;

    public ProjectService(ProjectRepository repository,
                          UserRepository userRepository,
                          TaskRepository taskRepository,
                          ActivityService activityService) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.activityService = activityService;
    }

    private User currentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user named " + username));
    }

    private Project requireProject(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
    }


    /** Create a project. It starts with NO leader; a manager assigns one next. */
    @Transactional
    public Project addProject(Project project, String managerUsername) {
        if (project.getTitle() == null || project.getTitle().isBlank()) {
            throw new ValidationException("Project title must not be empty");
        }
        project.setLeader(null); // leadership is assigned separately, never on create
        Project saved = repository.save(project);
        activityService.record(managerUsername, ActivityType.PROJECT_CREATED, saved.getId(),
                "Created project '" + saved.getTitle() + "'");
        return saved;
    }

    @Transactional
    public Project updateAsManager(Long id, Project data, String managerUsername) {
        Project existing = requireProject(id);
        if (data.getTitle() == null || data.getTitle().isBlank()) {
            throw new ValidationException("Project title must not be empty");
        }
        existing.setTitle(data.getTitle());
        existing.setDescription(data.getDescription());
        if (data.getStatus() != null) {
            existing.setStatus(data.getStatus());
        }
        existing.setDueDate(data.getDueDate());
        Project saved = repository.save(existing);
        activityService.record(managerUsername, ActivityType.PROJECT_UPDATED, saved.getId(),
                "Updated project '" + saved.getTitle() + "'");
        return saved;
    }

    /** Delete a project safely: detach its tasks and clear its team first so we
     *  never hit the tasks.project_id / project_team foreign keys. */
    @Transactional
    public void deleteAsManager(Long id, String managerUsername) {
        Project project = requireProject(id);
        String title = project.getTitle();

        List<Task> tasks = taskRepository.findByProject(project);
        for (Task t : tasks) {
            t.setProject(null); // keep the task, just unlink it from the project
        }
        taskRepository.saveAll(tasks);

        project.getTeamMembers().clear(); // clears the project_team rows
        repository.save(project);
        repository.delete(project);

        activityService.record(managerUsername, ActivityType.PROJECT_DELETED, id,
                "Deleted project '" + title + "'");
    }

    /** Assign an existing user as the leader of a project (manager only). */
    @Transactional
    public Project assignLeader(Long projectId, String username, String managerUsername) {
        Project project = requireProject(projectId);
        User leader = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user named " + username));
        if (leader.getRole() != Role.USER) {
            throw new ValidationException("Only a  USER can be made a project leader");
        }
        project.setLeader(leader);
        project.getTeamMembers().add(leader); // a leader is also on the team
        Project saved = repository.save(project);
        activityService.record(managerUsername, ActivityType.LEADER_ASSIGNED, saved.getId(),
                "Assigned '" + username + "' as leader of '" + saved.getTitle() + "'");
        return saved;
    }

    @Transactional
    public Project revokeLeader(Long projectId, String managerUsername) {
        Project project = requireProject(projectId);
        project.setLeader(null);
        Project saved = repository.save(project);
        activityService.record(managerUsername, ActivityType.LEADER_REVOKED, saved.getId(),
                "Revoked the leader of '" + saved.getTitle() + "'");
        return saved;
    }

    @Transactional
    public Project changeStatus(Long id, Status status, String leaderUsername) {
        Project project = repository.findByIdAndLeader(id, currentUser(leaderUsername))
                .orElseThrow(() -> new ProjectNotFoundException(id));
        project.setStatus(status);
        Project saved = repository.save(project);
        activityService.record(leaderUsername, ActivityType.PROJECT_STATUS_CHANGED, saved.getId(),
                "Set project '" + saved.getTitle() + "' to " + status);
        return saved;
    }

    @Transactional
    public Project addTeamMember(Long projectId, String targetUsername, String leaderUsername) {
        Project project = repository.findByIdAndLeader(projectId, currentUser(leaderUsername))
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
        User newMember = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + targetUsername));
        project.getTeamMembers().add(newMember);
        Project saved = repository.save(project);
        activityService.record(leaderUsername, ActivityType.MEMBER_ADDED, saved.getId(),
                "Added '" + targetUsername + "' to '" + saved.getTitle() + "'");
        return saved;
    }

    @Transactional
    public Project removeTeamMember(Long projectId, String targetUsername, String leaderUsername) {
        Project project = repository.findByIdAndLeader(projectId, currentUser(leaderUsername))
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
        User member = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + targetUsername));
        project.getTeamMembers().remove(member);
        Project saved = repository.save(project);
        activityService.record(leaderUsername, ActivityType.MEMBER_REMOVED, saved.getId(),
                "Removed '" + targetUsername + "' from '" + saved.getTitle() + "'");
        return saved;
    }

    /** Tasks of one of MY led projects, looked up by title. */
    @Transactional(readOnly = true)
    public List<TaskDTO> getAllTasks(String leaderUsername, String projectTitle) {
        Optional<Project> project = repository.findByLeader(currentUser(leaderUsername)).stream()
                .filter(p -> p.getTitle().equalsIgnoreCase(projectTitle))
                .findFirst();
        if (project.isEmpty()) {
            return List.of();
        }
        return taskRepository.findByProject(project.get()).stream()
                .map(TaskDTO::from)
                .toList();
    }


    // Projects the caller must be a leader
    public List<Project> getAll(String username) {
        return repository.findByLeader(currentUser(username));
    }

    // Projects the caller is a MEMBER of
    @Transactional(readOnly = true)
    public List<Project> getMemberProjects(String username) {
        return repository.findByTeamMembersContains(currentUser(username));
    }

    // One project the caller LEADS (404 if missing or not theirs)
    public Project getById(Long id, String username) {
        return repository.findByIdAndLeader(id, currentUser(username))
                .orElseThrow(() -> new ProjectNotFoundException(id));
    }

    //Every project (manager/admin oversight)
    public List<Project> getAllProjects() {
        return repository.findAll();
    }

    // A project's team members as a safe DTO (manager/admin oversight).
    @Transactional(readOnly = true)
    public List<UserDTO> getTeam(Long projectId) {
        Project project = requireProject(projectId);
        return project.getTeamMembers().stream().map(UserDTO::from).toList();
    }

    // Any project's tasks (manager/admin oversight)
    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksOfProject(Long projectId) {
        Project project = requireProject(projectId);
        return taskRepository.findByProject(project).stream().map(TaskDTO::from).toList();
    }

    // Any project's completion percentage (manager/admin oversight).
    @Transactional(readOnly = true)
    public int progressOfProject(Long projectId) {
        Project project = requireProject(projectId);
        return percentDone(taskRepository.findByProject(project));
    }

    private int percentDone(List<Task> tasks) {
        if (tasks.isEmpty()) return 0;
        long done = tasks.stream().filter(t -> t.getStatus() == Status.DONE).count();
        return (int) Math.round(done * 100.0 / tasks.size());
    }

    public List<Project> search(String keyword, String username) {
        String key = keyword.toLowerCase();
        return repository.findByLeader(currentUser(username)).stream()
                .filter(p -> p.getTitle().toLowerCase().contains(key)
                        || (p.getDescription() != null && p.getDescription().toLowerCase().contains(key)))
                .toList();
    }

    // Search across ALL projects (manager/admin)
    public List<Project> searchAll(String keyword) {
        String key = keyword.toLowerCase();
        return repository.findAll().stream()
                .filter(p -> p.getTitle().toLowerCase().contains(key)
                        || (p.getDescription() != null && p.getDescription().toLowerCase().contains(key)))
                .toList();
    }

    public List<Project> overdue(LocalDate today, String username) {
        return repository.findByLeader(currentUser(username)).stream()
                .filter(p -> p.isOverdue(today))
                .sorted(Comparator.comparing(Project::getDueDate))
                .toList();
    }

    public Map<Status, Long> countByStatus(String username) {
        return repository.findByLeader(currentUser(username)).stream()
                .collect(Collectors.groupingBy(Project::getStatus, Collectors.counting()));
    }

}
