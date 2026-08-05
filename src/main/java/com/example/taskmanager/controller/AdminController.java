package com.example.taskmanager.controller;
import com.example.taskmanager.dto.ProjectDTO;
import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.service.AdminService;
import com.example.taskmanager.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

     final private  UserRepository userRepository;
     final private  TaskRepository taskRepository;
     final private  AdminService adminService;
     final private ProjectService projectService;

    protected AdminController(UserRepository userRepository, TaskRepository taskRepository, AdminService adminService,ProjectService projectService) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.adminService = adminService;
        this.projectService = projectService;
    }

    // GET /admin/users -> every account as a safe UserView (id, username, role)
    @GetMapping
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public List<com.example.taskmanager.dto.UserDTO> listUsers() {
        return adminService.listUsers();
    }

    // DELETE /admin/users/5 -> remove that account and its tasks. 204 = success.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_USER')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{id}/leader/{username}")
    @PreAuthorize("hasAuthority('LEADER_ASSIGN')")
    public ResponseEntity<Project> assignLeader(@PathVariable Long id, @PathVariable String username) {
        return ResponseEntity.ok(adminService.assignLeader(id, username));
    }
    @GetMapping("/projects")
    @PreAuthorize("hasAuthority('PROJECT_VIEW_ALL')")
    public List<ProjectDTO> ListAllProject(){
        return projectService.getAllProjects().stream().map(ProjectDTO::from).toList();
    }


}