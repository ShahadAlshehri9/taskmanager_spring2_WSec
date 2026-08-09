package com.example.taskmanager.controller;
import com.example.taskmanager.dto.ProjectDTO;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.dto.UserDTO;
import com.example.taskmanager.model.Project;
import com.example.taskmanager.model.Role;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.service.AdminService;
import com.example.taskmanager.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // GET /admin/users -> every account as a safe DTO
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public List<UserDTO> listUsers() {
        return adminService.listUsers();
    }

    // DELETE /admin/users/5 -> remove an account and everything it references
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH /admin/users/5/role?role=MANAGER -> change a user's role
    @PatchMapping("/users/{id}/role")
    @PreAuthorize("hasAuthority('USER_ROLE_SET')")
    public UserDTO changeRole(@PathVariable Long id, @RequestParam Role role) {
        return adminService.changeRole(id, role);
    }
}



