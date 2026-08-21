package com.example.taskmanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Behavioral tests for TaskFlow. These describe the *intended* behavior.
 * Fix the source in TaskManager.java until they all pass — do not change the tests.
 */
class TaskManagerTest {

    @Test
    void addTaskReturnsTaskWithIncrementingIds() {
        TaskManager tm = new TaskManager();
        Task a = tm.addTask("write report");
        Task b = tm.addTask("review PR");
        assertEquals(1, a.getId());
        assertEquals(2, b.getId());
        assertEquals("write report", a.getTitle());
    }

    @Test
    void tagsAreIndependent() {
        // Two tasks added without tags must not share the same tag list.
        TaskManager tm = new TaskManager();
        Task a = tm.addTask("task a");
        Task b = tm.addTask("task b");
        a.addTag("urgent");
        assertEquals(List.of("urgent"), a.getTags());
        assertTrue(b.getTags().isEmpty());
    }

    @Test
    void completeTaskMarksCompleted() {
        TaskManager tm = new TaskManager();
        tm.addTask("write report");
        Task task = tm.completeTask(1);
        assertTrue(task.isCompleted());
        assertTrue(tm.getTask(1).isCompleted());
    }

    @Test
    void getPendingExcludesCompleted() {
        TaskManager tm = new TaskManager();
        tm.addTask("a");
        tm.addTask("b");
        tm.getTask(1).setCompleted(true);
        List<String> pending = tm.getPending().stream()
                .map(Task::getTitle)
                .collect(Collectors.toList());
        assertEquals(List.of("b"), pending);
    }

    @Test
    void sortByPriorityHighFirst() {
        TaskManager tm = new TaskManager();
        tm.addTask("low", 1, new ArrayList<>());
        tm.addTask("high", 3, new ArrayList<>());
        tm.addTask("medium", 2, new ArrayList<>());
        List<String> ordered = tm.sortByPriority().stream()
                .map(Task::getTitle)
                .collect(Collectors.toList());
        assertEquals(List.of("high", "medium", "low"), ordered);
    }

    @Test
    void filterByTag() {
        TaskManager tm = new TaskManager();
        tm.addTask("a", 1, new ArrayList<>(List.of("work")));
        tm.addTask("b", 1, new ArrayList<>(List.of("home")));
        tm.addTask("c", 1, new ArrayList<>(List.of("work", "urgent")));
        List<String> titles = tm.filterByTag("work").stream()
                .map(Task::getTitle)
                .sorted()
                .collect(Collectors.toList());
        assertEquals(List.of("a", "c"), titles);
    }
}
