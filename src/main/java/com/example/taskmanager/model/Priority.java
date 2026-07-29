package com.example.taskmanager.model;

/** Priority level of a task. The weight is used for sorting. */
public enum Priority {
    LOW(1), MEDIUM(2), HIGH(3);

    private final int weight;

    Priority(int weight) {
        this.weight = weight;
    }

    public int weight() {
        return weight;
    }
}
