package com.example.taskmanager.Config;

import com.example.taskmanager.Config.ActivityType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
@Setter
@Getter
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;      // who acted
    @Enumerated(EnumType.STRING)
    private ActivityType type;
    private String entityType;    // "TASK" | "PROJECT"
    private Long entityId;        // id of the affected task/project
    private String summary;       // human text example--> "Marked task 'Deploy' as DONE"
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { this.createdAt = LocalDateTime.now(); }
}