package com.example.taskmanager;

import java.util.List;

/**
 * A single task. Priority scale: 1 = low, 2 = medium, 3 = high.
 */
public class Task {
    private final int id;
    private final String title;
    private final int priority;
    private final List<String> tags;
    private boolean completed;

    public Task(int id, String title, int priority, List<String> tags) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.tags = tags;
        this.completed = false;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getPriority() {
        return priority;
    }

    public List<String> getTags() {
        return tags;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    @Override
    public String toString() {
        return String.format("Task(#%d %s p%d %s)",
                id, title, priority, completed ? "done" : "pending");
    }
}
