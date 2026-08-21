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
 *
 * There are 6 planted bugs: 4 are easy to spot from a single failing test, and 2 are
 * subtler (they only bite on an edge case). The tests are grouped accordingly.
 */
class TaskManagerTest {

    // -----------------------------------------------------------------------
    // The 4 easier bugs
    // -----------------------------------------------------------------------

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
    void countReflectsNumberOfTasks() {
        TaskManager tm = new TaskManager();
        assertEquals(0, tm.count());
        tm.addTask("a");
        tm.addTask("b");
        assertEquals(2, tm.count());
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

    // -----------------------------------------------------------------------
    // The 2 harder bugs (edge cases)
    // -----------------------------------------------------------------------

    @Test
    void tasksAddedWithoutTagsDoNotShareAList() {
        // Adding a tag to one task must not affect another task added without tags.
        TaskManager tm = new TaskManager();
        Task a = tm.addTask("task a");
        Task b = tm.addTask("task b");
        a.addTag("urgent");
        assertEquals(List.of("urgent"), a.getTags());
        assertTrue(b.getTags().isEmpty());
    }

    @Test
    void idsAreNeverReusedAfterRemoval() {
        // After removing a task, a newly added task must get a fresh, unique id.
        TaskManager tm = new TaskManager();
        tm.addTask("a");            // id 1
        tm.addTask("b");            // id 2
        tm.addTask("c");            // id 3
        tm.removeTask(2);           // remove the middle one
        Task d = tm.addTask("d");   // must NOT collide with the existing id 3
        assertEquals(4, d.getId());
    }

    // -----------------------------------------------------------------------
    // Correct helper (kept as a clean reference / used by the feature half)
    // -----------------------------------------------------------------------

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
