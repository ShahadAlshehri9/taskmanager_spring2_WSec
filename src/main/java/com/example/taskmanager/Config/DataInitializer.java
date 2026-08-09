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

    @Value("${app.admin.username:admin}")
    private String adminUsername;
    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.manager.username:manager}")
    private String managerUsername;
    @Value("${app.manager.password:manager123}")
    private String managerPassword;

    @Bean
    public CommandLineRunner seedAccounts(UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (!repo.existsByUsername(adminUsername)) {
                repo.save(new User(adminUsername, encoder.encode(adminPassword), Role.ADMIN));
            }
            if (!repo.existsByUsername(managerUsername)) {
                repo.save(new User(managerUsername, encoder.encode(managerPassword), Role.MANAGER));
            }
        };
    }
}