package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ProjectDTO;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.Status;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.service.ProjectService;
import com.example.taskmanager.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final TaskService taskService;

    public ProjectController(ProjectService projectService, TaskService taskService) {
        this.projectService = projectService;
        this.taskService = taskService;
    }

    // GET /projects -> projects I LEAD
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<ProjectDTO> myLedProjects(Principal principal) {
        return projectService.getAll(principal.getName()).stream().map(ProjectDTO::from).toList();
    }

    // GET /projects/member -> projects I am a MEMBER of
    @GetMapping("/member")
    @PreAuthorize("hasAuthority('PROJECT_VIEW_MEMBER')")
    public List<ProjectDTO> myMemberProjects(Principal principal) {
        return projectService.getMemberProjects(principal.getName()).stream().map(ProjectDTO::from).toList();
    }

    // GET /projects/5 -> one project I lead
    @GetMapping("/{id}")
    @PreAuthorize("@projectSecurity.isLeader(#id, authentication.name)")
    public ProjectDTO getProjectById(@PathVariable Long id, Principal principal) {
        return ProjectDTO.from(projectService.getById(id, principal.getName()));
    }

    // PATCH /projects/5/status?status=DONE -> change status (leader)
    @PatchMapping("/{id}/status")
    @PreAuthorize("@projectSecurity.isLeader(#id, authentication.name)")
    public ProjectDTO changeStatus(@PathVariable Long id, @RequestParam Status status, Principal principal) {
        return ProjectDTO.from(projectService.changeStatus(id, status, principal.getName()));
    }

    // POST /projects/5/team/ali -> add "ali" to project 5 (leader)
    @PostMapping("/{id}/team/{targetUsername}")
    @PreAuthorize("@projectSecurity.isLeader(#id, authentication.name)")
    public ProjectDTO addTeamMember(@PathVariable Long id, @PathVariable String targetUsername, Principal principal) {
        return ProjectDTO.from(projectService.addTeamMember(id, targetUsername, principal.getName()));
    }

    // DELETE /projects/5/team/ali -> remove "ali" from project 5 (leader)
    @DeleteMapping("/{id}/team/{targetUsername}")
    @PreAuthorize("@projectSecurity.isLeader(#id, authentication.name)")
    public ProjectDTO removeTeamMember(@PathVariable Long id, @PathVariable String targetUsername, Principal principal) {
        return ProjectDTO.from(projectService.removeTeamMember(id, targetUsername, principal.getName()));
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

    // GET /projects/search?keyword=api -> search my led projects
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public List<ProjectDTO> searchProjects(@RequestParam String keyword, Principal principal) {
        return projectService.search(keyword, principal.getName()).stream().map(ProjectDTO::from).toList();
    }

    // GET /projects/overdue -> my overdue led projects
    @GetMapping("/overdue")
    @PreAuthorize("isAuthenticated()")
    public List<ProjectDTO> getOverdueProjects(Principal principal) {
        return projectService.overdue(LocalDate.now(), principal.getName()).stream().map(ProjectDTO::from).toList();
    }

    // GET /projects/statusCount -> count my led projects by status
    @GetMapping("/statusCount")
    @PreAuthorize("isAuthenticated()")
    public Map<Status, Long> countProjectsByStatus(Principal principal) {
        return projectService.countByStatus(principal.getName());
    }
  }