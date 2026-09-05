package com.example.taskmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * TaskFlow — a tiny in-memory task tracker.
 *
 * The Javadoc on each method describes what it is *supposed* to do. The behavioral tests
 * in TaskManagerTest check that intent. Some implementations don't match their
 * description — those are the bugs you're here to find.
 */
public class TaskManager {

    private final List<Task> tasks = new ArrayList<>();

    // The tag list a task gets when the caller doesn't supply one.
    private static final List<String> DEFAULT_TAGS = new ArrayList<>();

    /**
     * Create a new task with no tags, store it, and return it. A task added without tags
     * should get its own, independent empty tag list.
     */
    public Task addTask(String title) {
        return addTask(title, 1, DEFAULT_TAGS);
    }

    /** Create a new task with the given priority and tags, store it, and return it. */
    public Task addTask(String title, int priority, List<String> tags) {
        Task task = new Task(nextId(), title, priority, tags);
        tasks.add(task);
        return task;
    }

    /**
     * Return a fresh id that is unique and never reused for the lifetime of this manager —
     * even after tasks have been removed.
     */
    private int nextId() {
        return tasks.size() + 1;
    }

    /** Remove the task with the given id (a no-op if it isn't present). */
    public void removeTask(int id) {
        tasks.removeIf(task -> task.getId() == id);
    }

    /** Return the task with the given id, or null if there isn't one. */
    public Task getTask(int id) {
        for (Task task : tasks) {
            if (task.getId() == id) {
                return task;
            }
        }
        return null;
    }

    /** Return how many tasks are currently tracked — completed or not. */
    public int count() {
        int n = 0;
        for (Task task : tasks) {
            if (!task.isCompleted()) {
                n++;
            }
        }
        return n;
    }

    /**
     * Find the task with the given id, mark it completed, and return it.
     *
     * Completing a task is idempotent: completing one that is already completed leaves it
     * completed.
     */
    public Task completeTask(int id) {
        Task task = getTask(id);
        task.setCompleted(!task.isCompleted());
        return task;
    }

    /**
     * Return every task that is NOT yet completed. This is a read-only query: it must not
     * modify the manager's stored task list.
     */
    public List<Task> getPending() {
        tasks.removeIf(Task::isCompleted);
        return tasks;
    }

    /**
     * Return the tasks ordered by priority, highest priority first. Tasks that share a
     * priority keep their original insertion order (a stable sort).
     */
    public List<Task> sortByPriority() {
        List<Task> sorted = new ArrayList<>(tasks);
        sorted.sort(Comparator.comparingInt(Task::getPriority));
        Collections.reverse(sorted);
        return sorted;
    }

    /** Return every task whose tags include the given tag. */
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
