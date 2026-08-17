package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ProjectDTO;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.model.Priority;
import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.Status;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.service.DashboardService;
import com.example.taskmanager.service.ProjectService;
import com.example.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;


import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final TaskService taskService;
    private final DashboardService dashboardService;

    public ProjectController(ProjectService projectService, TaskService taskService, DashboardService dashboardService) {
        this.projectService = projectService;
        this.taskService = taskService;
        this.dashboardService = dashboardService;
    }

    // GET /projects -> projects I LEAD
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<ProjectDTO> myLedProjects(Principal principal) {
        return projectService.getAll(principal.getName())
                .stream()
                .map(project -> {
                    int progress = dashboardService.projectProgress(project.getId(), principal.getName());
                    return ProjectDTO.from(project, progress);
                })
                .toList();
    }

    // GET /projects/member -> projects I am a MEMBER of
    @GetMapping("/member")
    @PreAuthorize("hasAuthority('PROJECT_VIEW_MEMBER')")
    public List<ProjectDTO> myMemberProjects(Principal principal) {
        return projectService.getMemberProjects(principal.getName())
                .stream()
                .map(project -> {
                    int progress = dashboardService.projectProgress(project.getId(), principal.getName());
                    return ProjectDTO.from(project, progress);
                })
                .toList();
    }

    @GetMapping("/member/{id}")
    @PreAuthorize("hasAuthority('PROJECT_VIEW_MEMBER')")
    public ResponseEntity<Project> getProject(@PathVariable Long id, Principal principal) {
        String username = principal.getName();
        Project project = projectService.getProjectByIdForMember(id, username);
        return ResponseEntity.ok(project);
    }

    // GET /projects/5 -> one project I lead
    @GetMapping("/{id}")
    @PreAuthorize("@projectSecurity.isLeader(#id, authentication.name) or isManager(authentication.name)")
    public ProjectDTO getProjectById(@PathVariable Long id, Principal principal) {
        Project project = projectService.getById(id, principal.getName());
        int progress = dashboardService.projectProgress(id, principal.getName());
        return ProjectDTO.from(project, progress);
    }

    // PATCH /projects/5/status?status=DONE -> change status
    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ProjectDTO changeStatus(@PathVariable Long id, @RequestParam Status status, Principal principal) {
        Project project = projectService.changeStatus(id, status, principal.getName());
        int progress = dashboardService.projectProgress(project.getId(), principal.getName());
        return ProjectDTO.from(project, progress);
    }

    // POST /projects/5/team/ali -> add "ali" to project 5 (leader)
    @PostMapping("/{id}/team/{targetUsername}")
    @PreAuthorize("@projectSecurity.isLeader(#id, authentication.name)")
    public ProjectDTO addTeamMember(@PathVariable Long id, @PathVariable String targetUsername, Principal principal) {
        Project project = projectService.addTeamMember(id, targetUsername, principal.getName());
        int progress = dashboardService.projectProgress(project.getId(), principal.getName());
        return ProjectDTO.from(project, progress);
    }

    // DELETE /projects/5/team/ali -> remove "ali" from project 5 (leader)
    @DeleteMapping("/{id}/team/{targetUsername}")
    @PreAuthorize("@projectSecurity.isLeader(#id, authentication.name)")
    public ProjectDTO removeTeamMember(@PathVariable Long id, @PathVariable String targetUsername, Principal principal) {
        Project project = projectService.removeTeamMember(id, targetUsername, principal.getName());
        int progress = dashboardService.projectProgress(project.getId(), principal.getName());
        return ProjectDTO.from(project, progress);
    }

    // PATCH /projects/5/assign/sara -> create a project task and assign it to "sara" (leader)
    @PatchMapping("/{id}/assign/{assigneeUsername}")
    @PreAuthorize("@projectSecurity.isLeader(#id, authentication.name)")
    public TaskDTO assignTask(@PathVariable Long id, @PathVariable String assigneeUsername,
                              @RequestBody Task task, Principal principal) {
        return TaskDTO.from(taskService.createTaskForProjectAndAssign(id, assigneeUsername, task, principal.getName()));
    }

    // GET /projects/{title}/tasks -> tasks of one of my led projects, by title
    @GetMapping("/{title}/tasks")
    @PreAuthorize("isAuthenticated()")
    public List<TaskDTO> getProjectTasks(@PathVariable String title, Principal principal) {
        return projectService.getAllTasks(principal.getName(), title);
    }

    // GET /projects/5/task-stats -> task counts by status
    @GetMapping("/{id}/task-stats")
    @PreAuthorize("hasAuthority('PROJECT_PROGRESS_VIEW_ALL') " +
            "or @projectSecurity.isLeader(#id, authentication.name) " +
            "or @projectSecurity.isMember(#id, authentication.name)")
    public Map<Status, Long> getTaskStatusCounts(@PathVariable Long id) {
        return projectService.taskStatusCountsForProject(id);
    }
    @GetMapping("/{id}/task-priority")
    @PreAuthorize("hasAuthority('PROJECT_PROGRESS_VIEW_ALL') " +
            "or @projectSecurity.isLeader(#id, authentication.name) " +
            "or @projectSecurity.isMember(#id, authentication.name)")
    public Map<Priority, Long> getTaskPriorityCounts(@PathVariable Long id) {
        return projectService.taskPriorityCountsForProject(id);
    }

    // GET /projects/search?keyword=api -> search my led projects
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public List<ProjectDTO> searchProjects(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) String filter,
                                           @RequestParam(required = false) String sort, Principal principal) {
        return projectService.search(keyword, filter, principal.getName(), sort)
                .stream()
                .map(project -> {
                    int progress = dashboardService.projectProgress(project.getId(), principal.getName());
                    return ProjectDTO.from(project, progress);
                })
                .toList();
    }

    // GET /projects/overdue -> my overdue led projects
    @GetMapping("/overdue")
    @PreAuthorize("isAuthenticated()")
    public List<ProjectDTO> getOverdueProjects(Principal principal) {
        return projectService.overdue(LocalDate.now(), principal.getName())
                .stream()
                .map(project -> {
                    int progress = dashboardService.projectProgress(project.getId(), principal.getName());
                    return ProjectDTO.from(project, progress);
                })
                .toList();
    }

    // GET /projects/statusCount -> count my led projects by status
    @GetMapping("/statusCount")
    @PreAuthorize("isAuthenticated()")
    public Map<Status, Long> countProjectsByStatus(Principal principal) {
        return projectService.countByStatus(principal.getName());
    }

    @PutMapping("{projectId}/task/{taskId}/update")
    @PreAuthorize("isAuthenticated()")
    public Task LeaderUpdateTask(@PathVariable Long projectId,
                                 @PathVariable Long taskId,
                                 @Valid @RequestBody Task task,
                                 Principal principal) {
        try {
            projectService.verifyProjectLeader(projectId, principal.getName());
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e);
        }
        return taskService.updateLeader(taskId, task, principal.getName());
    }
}