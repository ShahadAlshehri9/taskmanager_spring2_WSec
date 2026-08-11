package com.example.taskmanager.service;

import com.example.taskmanager.dto.UserDTO;
import com.example.taskmanager.exception.GlobalExceptionHandler;
import com.example.taskmanager.exception.ValidationException;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    public final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }
    @Transactional
    public UserDTO updateUsername(String username, String newUsername) {
        // 1. Fetch the user by their Primary Key (ID)
        User user = repository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("User not found"));

        // 2. Validate the input
        if (newUsername == null || newUsername.trim().isEmpty()) {
            throw new ValidationException("Username must contain at least one character");
        }

        // (Ensure we don't throw an error if the user is just submitting their current username)
        if (!user.getUsername().equalsIgnoreCase(newUsername) && repository.existsByUsername(newUsername)) {
            throw new ValidationException("Username is already taken");
        }

        // 4. Update the entity
        user.setUsername(newUsername);

        // Because of @Transactional, you don't even need repository.save(user).
        // Spring will automatically update the database when the method finishes.
        return UserDTO.from(user);
    }
}

