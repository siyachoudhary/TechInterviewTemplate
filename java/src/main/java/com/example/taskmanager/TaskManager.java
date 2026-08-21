package com.example.taskmanager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * TaskFlow — a tiny in-memory task tracker.
 */
public class TaskManager {

    private final List<Task> tasks = new ArrayList<>();
    private int nextId = 1;

    // Reused as the default tag list when a caller doesn't supply tags.
    private static final List<String> DEFAULT_TAGS = new ArrayList<>();

    public Task addTask(String title) {
        return addTask(title, 1, DEFAULT_TAGS);
    }

    public Task addTask(String title, int priority, List<String> tags) {
        Task task = new Task(nextId, title, priority, tags);
        tasks.add(task);
        nextId++;
        return task;
    }

    public Task getTask(int id) {
        for (Task task : tasks) {
            if (task.getId() == id) {
                return task;
            }
        }
        return null;
    }

    public Task completeTask(int id) {
        Task task = tasks.get(id);
        task.setCompleted(true);
        return task;
    }

    public List<Task> getPending() {
        List<Task> pending = new ArrayList<>();
        for (Task task : tasks) {
            if (task.isCompleted()) {
                pending.add(task);
            }
        }
        return pending;
    }

    public List<Task> sortByPriority() {
        List<Task> sorted = new ArrayList<>(tasks);
        sorted.sort(Comparator.comparingInt(Task::getPriority));
        return sorted;
    }

    public List<Task> filterByTag(String tag) {
        List<Task> matches = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getTags().contains(tag)) {
                matches.add(task);
            }
        }
        return matches;
    }
}
