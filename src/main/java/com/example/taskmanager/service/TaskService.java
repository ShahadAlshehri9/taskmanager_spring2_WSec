package com.example.taskmanager.service;

import com.example.taskmanager.model.ActivityType;
import com.example.taskmanager.exception.ProjectNotFoundException;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.exception.ValidationException;
import com.example.taskmanager.model.*;
import com.example.taskmanager.repository.ProjectRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;//lets you sort the same objects in multiple, different ways without changing the original class code.
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/* Business logic. Depends on the Repository interface, not the concrete
 implementation, so it survives the Week 2 switch to a database.
 The query methods are where lambdas and streams get exercised.

 SECURITY: every public method takes the logged-in username and works ONLY on
 that user's tasks - currentUser() turns the name into the owning User, and all
 queries go through the owner-aware repository methods.
 */
// SPRING NOTE: @Service marks this as a "bean" Spring creates and manages.
// Spring sees the repository parameters in the constructor and passes them in
// automatically - that is Dependency Injection (DI). You never call `new` on it.
@Service //Used in the service layer to define business logic and improve code readability.
public class TaskService {

    private final TaskRepository repository; //interface (now a Spring Data JPA repository)
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ActivityService activityService;
    private final ProjectService projectService;

    public TaskService(TaskRepository repository, UserRepository userRepository, ProjectRepository projectRepository,
                        ActivityService activityService, ProjectService projectService) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.projectRepository=projectRepository;
        this.activityService = activityService;
        this.projectService = projectService;
    }

    // Helper: turn the logged-in username into the User row that owns the tasks.
    private User currentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user named " + username));
    }

    public Task add(Task task, String username) {
        if (task.getTitle() == null || task.getTitle().isBlank()) {
            throw new ValidationException("Task title must not be empty");
        }
        if (task.getProject() != null) {
            throw new ValidationException("Personal tasks cannot belong to a project.");
        }
        task.setOwner(currentUser(username));   // stamp the owner before saving
        Task saved = repository.save(task);
        activityService.record(username, ActivityType.TASK_CREATED, saved.getId(),
                "Created task '" + saved.getTitle() + "'");
        return saved;
    }
    /*Stream is a way of going through a collection of data such that
    the programmer determines the operation to be performed on each value.
    No record is kept of the index or the variable being processed at any given time.*/
    public Task getById(Long id, String username) {
        // only returns the task if THIS user owns it; otherwise same 404 as missing
        return repository.findByIdAndOwner(id, currentUser(username))
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    public List<Task> getAll(String username) {
        return repository.findByOwner(currentUser(username));   // was findAll()
    }

    public Task changeStatus(Long id, Status status, String username) {
        Task task = getById(id, username);   // owner-checked
        task.setStatus(status);
        Task saved = repository.save(task);
        activityService.record(username, ActivityType.TASK_STATUS_CHANGED, id,
                "Marked '" + saved.getTitle() + "' as " + status);
        if (saved.getProject() != null) {
            projectService.syncStatusFromProgress(saved.getProject(), username);
        }
        return saved;
    }
    public List<Task> getAllTasks(String username,String ProjectTitle){
        return repository.findByOwner(currentUser(username)).stream().filter(t-> t.getProject().getTitle().equalsIgnoreCase(ProjectTitle)).toList();
    }
    // Replaces the whole task when editing (used by the PUT endpoint).
    public Task update(Long id, Task data, String username) {
        Task existing = getById(id, username); // throws TaskNotFoundException (-> 404) if missing or not owned
        if (data.getTitle() == null || data.getTitle().isBlank()) {
            throw new ValidationException("Task title must not be empty");
        }
        existing.setTitle(data.getTitle());
        existing.setDescription(data.getDescription());
        existing.setPriority(data.getPriority());
        if (data.getStatus() != null) {
            existing.setStatus(data.getStatus());
        }
        existing.setDueDate(data.getDueDate());
        Task saved = repository.save(existing);
        activityService.record(username,ActivityType.TASK_UPDATED, saved.getId(),"The"+saved.getTitle()+" Task has been Updated");
        if (saved.getProject() != null) {
            projectService.syncStatusFromProgress(saved.getProject(), username);
        }
        return saved;
    }

    public void delete(Long id, String username) {
        // only delete if that id exists AND belongs to this user
        if (!repository.existsByIdAndOwner(id, currentUser(username))) {
            throw new TaskNotFoundException(id);
        }
        Optional<Task> task = repository.findByIdAndOwner(id,currentUser(username));
        Project project = task.get().getProject();
        activityService.record(username,ActivityType.TASK_DELETED, id," the "+ task.get().getTitle() +" has been deleted");
        repository.deleteById(id);
        if (project != null) {
            projectService.syncStatusFromProgress(project, username);
        }
    }

    /*An event chain may include dumping some of the values, converting values
    from one form to another, or calculations. A stream does not change the values in the original data collection,
    but merely processes them.If you want to retain the transformations,
    they need to be compiled into another data collection.*/
    public List<Task> byStatus(Status status, String username) {

         return repository.findByOwner(currentUser(username)).stream()
                .filter(t -> t.getStatus() == status)//filter (value -> filter condition) return only the value that matches the condition
                .toList();//to store the results in a list
    }
    /*t-> t.getStatus() lambda expression
    is shorthand provided by Java for anonymous methods that do not have an "owner", i.e., they are not part of a class or an interface.
        The function contains both the parameter definition and the function body.*/
    public List<Task> byPriority(Priority priority, String username) {
        return repository.findByOwner(currentUser(username)).stream()//The method is called on collection that implements the Collection interface, such as an ArrayList Object.
                .filter(t -> t.getPriority() == priority)//The elements that do not satisfy the filter condition are removed from the string
                .toList();
    }

    public List<Task> search(String keyword, String status, String username) {
        String key = keyword != null ? keyword.toLowerCase() : "";

        return repository.findByOwner(currentUser(username)).stream()
                .filter(t -> t.getTitle().toLowerCase().contains(key)
                        || (t.getDescription() != null && t.getDescription().toLowerCase().contains(key)))
                .filter(t -> matchesStatus(t, status))
                .toList();
    }

    private boolean matchesStatus(Task task, String status) {
        if (status == null || status.trim().isEmpty()) {
            return true;} if (task.getStatus() == null) {
            return false;}
        return task.getStatus().name().equalsIgnoreCase(status);
    }

    public List<Task> overdue(LocalDate today, String username) {
        return repository.findByOwner(currentUser(username)).stream()
                .filter(t -> t.isOverdue(today))
                .sorted(Comparator.comparing(Task::getDueDate))
                .toList();
    }

    public List<Task> sortedByPriority(String username) {
        return repository.findByOwner(currentUser(username)).stream()
                .sorted(Comparator.comparingInt((Task t) -> t.getPriority().weight()).reversed())
                .toList();
    }

    public Map<Status, Long> countByStatus(String username) {
        return repository.findByOwner(currentUser(username)).stream()
                .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));//creates a new map object that holds the collected values.
    }

    public Task createTaskForProjectAndAssign(Long projectId, String assigneeUsername, Task taskRequest, String leaderUsername) {
        // step one find the project
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        // next we will verify the person making the request is the Project Leader
        if (!project.getLeader().getUsername().equals(leaderUsername)) {
            throw new ValidationException("Only the project leader can create and assign tasks for this project.");
        }

        // validate user assigned to existing find the user being assigned
        User assignee = userRepository.findByUsername(assigneeUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User " + assigneeUsername + " not found"));

        //  its team member ? Verify the assignee is actually in the project's team list
        if (!project.getTeamMembers().contains(assignee)) {
            throw new ValidationException("Cannot assign task. User is not a team member of this project.");
        }

        // last link the task to the project and the user
        taskRequest.setProject(project);
        taskRequest.setOwner(assignee);

        // Save and return the new task
        Task saved = repository.save(taskRequest);
        activityService.record(leaderUsername,ActivityType.PROJECT_TASK_ASSIGNED, saved.getId() ," the task "+ saved.getTitle() +" has been Assigned to"+ assigneeUsername);
        activityService.record(leaderUsername,ActivityType.PROJECT_TASK_CREATED, saved.getId() ," the task "+ saved.getTitle() +" has been Created");
        projectService.syncStatusFromProgress(project, leaderUsername);
        return saved;
    }
    /*    public Task update(Long id, Task data, String username) {
        Task existing = getById(id, username); // throws TaskNotFoundException (-> 404) if missing or not owned
        if (data.getTitle() == null || data.getTitle().isBlank()) {
            throw new ValidationException("Task title must not be empty");
        }
        existing.setTitle(data.getTitle());
        existing.setDescription(data.getDescription());
        existing.setPriority(data.getPriority());
        if (data.getStatus() != null) {
            existing.setStatus(data.getStatus());
        }
        existing.setDueDate(data.getDueDate());
        Task saved = repository.save(existing);
        activityService.record(username,ActivityType.TASK_UPDATED, saved.getId(),"The"+saved.getTitle()+" Task has been Updated");
        return saved;
    }*/
    //for leader to update a task
    public Task updateLeader(Long TaskId, Task T, String username ){
        Task exist = repository.findById(TaskId).orElseThrow(()->new TaskNotFoundException(TaskId));
        if (T.getTitle() == null || T.getTitle().isBlank()) {
            throw new ValidationException("Task title must not be empty");
        }
        exist.setTitle(T.getTitle());
        exist.setDescription(T.getDescription());
        exist.setPriority(T.getPriority());
        if (T.getStatus() != null) {
            exist.setStatus(T.getStatus());
        }
        exist.setDueDate(T.getDueDate());
        Task saved = repository.save(exist);
        activityService.record(username,ActivityType.TASK_UPDATED, saved.getId(),"The"+saved.getTitle()+" Task has been Updated");
        if (saved.getProject() != null) {
            projectService.syncStatusFromProgress(saved.getProject(), username);
        }
        return saved;

    }


}