package com.example.taskmanager.Config;

import com.example.taskmanager.model.Role;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Value("${app.admin.username:admin}")   // from application.properties, default "admin"
    private String adminUsername;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Bean
    public CommandLineRunner seedAdmin(UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (!repo.existsByUsername(adminUsername)) {   // only the first time
                repo.save(new User(adminUsername, encoder.encode(adminPassword), Role.ADMIN));
            }
        };
    }
}