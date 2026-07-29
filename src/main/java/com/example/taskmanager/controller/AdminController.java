package com.example.taskmanager.controller;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
public class AdminController {

     final private  UserRepository userRepository;
     final private  TaskRepository taskRepository;
     final private  AdminService adminService;

    protected AdminController(UserRepository userRepository, TaskRepository taskRepository, AdminService adminService) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.adminService = adminService;
    }

    // GET /admin/users -> every account as a safe UserView (id, username, role)
    @GetMapping
    public List<com.example.taskmanager.dto.UserDTO> listUsers() {
        return adminService.listUsers();
    }

    // DELETE /admin/users/5 -> remove that account and its tasks. 204 = success.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}