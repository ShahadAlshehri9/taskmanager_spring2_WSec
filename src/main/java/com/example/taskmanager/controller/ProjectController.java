package com.example.taskmanager.controller;

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

    public ProjectController(ProjectService projectService,TaskService taskService) {
        this.projectService = projectService;
        this.taskService = taskService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Project> createProject(@RequestBody Project project, Principal principal) {
        Project created = projectService.addProject(project, principal.getName());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Project>> getAllProjects(Principal principal) {
        return ResponseEntity.ok(projectService.getAll(principal.getName()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(projectService.getById(id, principal.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Project> updateProject(
            @PathVariable Long id,
            @RequestBody Project data, // Fixed: Using Project here instead of Task
            Principal principal) {
        Project updated = projectService.update(id, data, principal.getName());
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated() or @projectSecurity.isLeader(#id, authentication.name)")
    public ResponseEntity<Project> changeStatus(
            @PathVariable Long id,
            @RequestParam Status status,
            Principal principal) {
        Project updated = projectService.changeStatus(id, status, principal.getName());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or @projectSecurity.isLeader(#id, authentication.name)")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id, Principal principal) {
        projectService.delete(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Project>> searchProjects(
            @RequestParam String keyword,
            Principal principal) {
        return ResponseEntity.ok(projectService.search(keyword, principal.getName()));
    }

    @GetMapping("/overdue")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Project>> getOverdueProjects(Principal principal) {
        return ResponseEntity.ok(projectService.overdue(LocalDate.now(), principal.getName()));
    }


    @GetMapping("/statusCount")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<Status, Long>> countProjectsByStatus(Principal principal) {
        return ResponseEntity.ok(projectService.countByStatus(principal.getName()));
    }

    @GetMapping("/{title}/tasks")
    @PreAuthorize("isAuthenticated() or @projectSecurity.isLeader(#id, authentication.name)")
    public ResponseEntity<List<Task>> getProjectTasks(
            @PathVariable String title,
            Principal principal) {
        return ResponseEntity.ok(projectService.getAllTasks(principal.getName(), title));
    }
    // POST /projects/5/team/ali -> Adds user "ali" to project 5
    @PostMapping("/{id}/team/{targetUsername}")
    @PreAuthorize("@projectSecurity.isLeader(#id, authentication.name)")
    public ResponseEntity<Project> addTeamMember(
            @PathVariable Long id,
            @PathVariable String targetUsername,
            Principal principal) {
        Project updated = projectService.addTeamMember(id, targetUsername, principal.getName());
        return ResponseEntity.ok(updated);
    }

    // DELETE /projects/5/team/ali -> Removes user "ali" from project 5
    @DeleteMapping("/{id}/team/{targetUsername}")
    @PreAuthorize("hasAuthority('PROJECT_MEMBER_REMOVE')")
    public ResponseEntity<Project> removeTeamMember(
            @PathVariable Long id,
            @PathVariable String targetUsername,
            Principal principal) {

        Project updated = projectService.removeTeamMember(id, targetUsername, principal.getName());
        return ResponseEntity.ok(updated);
    }
    //Long projectId, String assigneeUsername, Task taskRequest, String leaderUsername
    // PATCH /tasks/10/assign/sara -> Assigns new task of project 10 to user "sara"
    @PatchMapping("/{id}/assign/{assigneeUsername}")
    @PreAuthorize("hasAuthority('PROJECT_TASK_ASSIGN')")
    public ResponseEntity<Task> assignTask(
            @PathVariable Long id,
            @PathVariable String assigneeUsername,
            @RequestBody Task task, Principal principal) {

        Task updated = taskService.createTaskForProjectAndAssign(id, assigneeUsername,task, principal.getName());
        return ResponseEntity.ok(updated);
    }


}