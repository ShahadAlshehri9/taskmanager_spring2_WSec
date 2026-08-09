package com.example.taskmanager.service;

import com.example.taskmanager.model.Activity;
import com.example.taskmanager.model.ActivityType;
import com.example.taskmanager.repository.ActivityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityService {
    private final ActivityRepository repo;
    public ActivityService(ActivityRepository repo) { this.repo = repo; }

    public void record(String username, ActivityType type, Long entityId, String summary) {
        Activity a = new Activity();
        a.setUsername(username);
        a.setType(type);
        a.setEntityType(type.entityType());   // <-- pulled from the enum, not passed in
        a.setEntityId(entityId);
        a.setSummary(summary);
        repo.save(a);
    }

    public List<Activity> recentForUser(String username, int limit) {
        return repo.findTop20ByUsernameOrderByCreatedAtDesc(username);
    }
}