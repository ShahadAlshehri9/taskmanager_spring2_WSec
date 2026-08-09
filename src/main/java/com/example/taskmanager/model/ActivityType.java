package com.example.taskmanager.model;

public enum ActivityType {
    // personal tasks
    TASK_CREATED("TASK"),
    TASK_UPDATED("TASK"),
    TASK_STATUS_CHANGED("TASK"),
    TASK_DELETED("TASK"),

    // project tasks (managed by the leader)
    PROJECT_TASK_CREATED("TASK"),
    PROJECT_TASK_ASSIGNED("TASK"),
    PROJECT_TASK_UPDATED("TASK"),
    PROJECT_TASK_DELETED("TASK"),

    // projects
    PROJECT_CREATED("PROJECT"),
    PROJECT_UPDATED("PROJECT"),
    PROJECT_STATUS_CHANGED("PROJECT"),
    PROJECT_DELETED("PROJECT"),
    MEMBER_ADDED("PROJECT"),
    MEMBER_REMOVED("PROJECT"),
    LEADER_ASSIGNED("PROJECT"),
    LEADER_REVOKED("PROJECT"),

    // admin / users
    USER_DELETED("USER");
    private final String entityType;
    ActivityType(String entityType) { this.entityType = entityType; }
    public String entityType() { return entityType; }
}