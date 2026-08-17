package com.example.taskmanager.controller;

import com.example.taskmanager.dto.UserDTO;
import com.example.taskmanager.dto.UserTaskDTO;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.PatchExchange;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
//i don't think it's good to inti in here
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<UserTaskDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @PatchMapping("/username")
    @PreAuthorize("isAuthenticated()")
    public UserDTO updateMyUsername(@RequestParam String newUsername, Principal principal) {
        // principal.getName() automatically gets the current logged-in user's username.
        // We pass this directly to the service, ensuring users can only change their OWN name.
        return userService.updateUsername(principal.getName(), newUsername);
    }

}
