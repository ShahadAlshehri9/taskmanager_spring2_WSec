package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ProjectDTO;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.dto.UserDTO;
import com.example.taskmanager.model.Project;
import com.example.taskmanager.service.AdminService;
import com.example.taskmanager.service.DashboardService;
import com.example.taskmanager.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/manager")
public class ManagerController {

    private final ProjectService projectService;
    private final AdminService adminService;
    private final DashboardService dashboardService;

    public ManagerController(ProjectService projectService, AdminService adminService, DashboardService dashboardService) {
        this.projectService = projectService;
        this.adminService = adminService;
        this.dashboardService = dashboardService;
    }

    // GET /manager/users -> every account as a safe UserView (id, username, role)
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public List<UserDTO> allUsers() {
        return adminService.listUsers();
    }

    // GET /manager/projects -> every project
    @GetMapping("/projects")
    @PreAuthorize("hasAuthority('PROJECT_VIEW_ALL')")
    public List<ProjectDTO> allProjects() {
        return projectService.getAllProjects().stream()
                .map(project -> {
                    int progress = dashboardService.projectProgressA(project.getId());
                    return ProjectDTO.from(project, progress);
                })
                .toList();
    }

    // GET /manager/projects/search?keyword=api -> search across all projects
    @GetMapping("/projects/search")
    @PreAuthorize("hasAuthority('PROJECT_VIEW_ALL')")
    public List<ProjectDTO> searchProjects(@RequestParam String keyword) {
        return projectService.searchAll(keyword).stream()
                .map(project -> {
                    int progress = dashboardService.projectProgressA(project.getId());
                    return ProjectDTO.from(project, progress);
                })
                .toList();
    }

    // GET /manager/projects/5/team -> a project's team members
    @GetMapping("/projects/{id}/team")
    @PreAuthorize("hasAuthority('TEAM_VIEW')")
    public List<UserDTO> team(@PathVariable Long id) {
        return projectService.getTeam(id);
    }

    // GET /manager/projects/5/tasks -> a project's tasks
    @GetMapping("/projects/{id}/tasks")
    @PreAuthorize("hasAuthority('PROJECT_TASK_VIEW_ALL')")
    public List<TaskDTO> projectTasks(@PathVariable Long id) {
        return projectService.getTasksOfProject(id);
    }

    // GET /manager/projects/5/progress -> a project's completion %
    @GetMapping("/projects/{id}/progress")
    @PreAuthorize("hasAuthority('PROJECT_PROGRESS_VIEW_ALL')")
    public int projectProgress(@PathVariable Long id) {
        return projectService.progressOfProject(id);
    }

    // ---------- project lifecycle (write) : MANAGER only ----------

    // POST /manager/projects -> create a project (leader assigned later)
    @PostMapping("/projects")
    @PreAuthorize("hasAuthority('PROJECT_CREATE')")
    public ResponseEntity<ProjectDTO> create(@RequestBody Project project, Principal principal) {
        Project created = projectService.addProject(project, principal.getName());
        int progress = dashboardService.projectProgressA(created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ProjectDTO.from(created, progress));
    }

    // PUT /manager/projects/5 -> update project details
    @PutMapping("/projects/{id}")
    @PreAuthorize("hasAuthority('PROJECT_UPDATE')")
    public ProjectDTO update(@PathVariable Long id, @RequestBody Project data, Principal principal) {
        Project updated = projectService.updateAsManager(id, data, principal.getName());
        int progress = dashboardService.projectProgressA(updated.getId());
        return ProjectDTO.from(updated, progress);
    }

    // DELETE /manager/projects/5 -> delete project (tasks are detached, not deleted)
    @DeleteMapping("/projects/{id}")
    @PreAuthorize("hasAuthority('PROJECT_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Principal principal) {
        projectService.deleteAsManager(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    // PATCH /manager/projects/5/leader/ali -> make "ali" the leader of project 5
    @PatchMapping("/projects/{id}/leader/{username}")
    @PreAuthorize("hasAuthority('LEADER_ASSIGN')")
    public ProjectDTO assignLeader(@PathVariable Long id, @PathVariable String username, Principal principal) {
        Project project = projectService.assignLeader(id, username, principal.getName());
        int progress = dashboardService.projectProgressA(project.getId());
        return ProjectDTO.from(project, progress);
    }

    // DELETE /manager/projects/5/leader -> remove the current leader of project 5
    @DeleteMapping("/projects/{id}/leader")
    @PreAuthorize("hasAuthority('LEADER_REVOKE')")
    public ProjectDTO revokeLeader(@PathVariable Long id, Principal principal) {
        Project project = projectService.revokeLeader(id, principal.getName());
        int progress = dashboardService.projectProgressA(project.getId());
        return ProjectDTO.from(project, progress);
    }
}