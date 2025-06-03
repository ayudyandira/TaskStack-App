package com.example.taskstackapp;

import java.io.Serializable;

public class Task implements Serializable {
    private String id;
    private String title;
    private String description;
    private String priority;
    private boolean completed;
    private long timestamp;
    private String userId;

    public Task() {
        // Required empty constructor for Firebase
    }

    public Task(String title, String description, String priority, String userId) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.userId = userId;
        this.completed = false;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
