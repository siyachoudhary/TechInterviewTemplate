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
 *
 * Each assertion carries a message describing the intended behavior, so a failure tells
 * you what the method is *supposed* to do — not just how two values differ.
 */
class TaskManagerTest {

    // -----------------------------------------------------------------------
    // The 4 easier bugs
    // -----------------------------------------------------------------------

    @Test
    void addTaskReturnsTaskWithIncrementingIds() {
        // Sanity check on the happy path: adding tasks assigns sequential ids (1, 2, ...)
        // and remembers the title. This one should pass out of the box.
        TaskManager tm = new TaskManager();
        Task a = tm.addTask("write report");
        Task b = tm.addTask("review PR");
        assertEquals(1, a.getId(), "the first task added should get id 1");
        assertEquals(2, b.getId(), "the second task added should get id 2 (ids increment by 1)");
        assertEquals("write report", a.getTitle(), "addTask should store the title it was given");
    }

    @Test
    void countReflectsNumberOfTasks() {
        // count() should simply report how many tasks are tracked: 0 when empty,
        // then 2 after adding two.
        TaskManager tm = new TaskManager();
        assertEquals(0, tm.count(), "count() on an empty manager should be 0");
        tm.addTask("a");
        tm.addTask("b");
        assertEquals(2, tm.count(), "count() should equal the number of tasks added (2 here)");
    }

    @Test
    void completeTaskMarksCompleted() {
        // Add one task (it gets id 1), then complete it BY ITS ID. Completing task id 1
        // should flip its completed flag to true, and that change should stick.
        TaskManager tm = new TaskManager();
        tm.addTask("write report");
        Task task = tm.completeTask(1);
        assertTrue(task.isCompleted(),
                "completeTask(id) should find the task with that id and mark it completed");
        assertTrue(tm.getTask(1).isCompleted(),
                "the completion must persist on the stored task, not just the returned copy");
    }

    @Test
    void getPendingExcludesCompleted() {
        // Add two tasks, mark the first ('a') completed directly, then ask for pending
        // work. "Pending" means not-yet-completed, so only 'b' should come back.
        TaskManager tm = new TaskManager();
        tm.addTask("a");
        tm.addTask("b");
        tm.getTask(1).setCompleted(true);
        List<String> pending = tm.getPending().stream()
                .map(Task::getTitle)
                .collect(Collectors.toList());
        assertEquals(List.of("b"), pending,
                "getPending() should return only NOT-completed tasks; 'a' was completed, so "
                        + "only 'b' should remain");
    }

    @Test
    void sortByPriorityHighFirst() {
        // Add tasks out of priority order. sortByPriority() should return them ordered by
        // priority with the HIGHEST (3) first, so: high, medium, low.
        TaskManager tm = new TaskManager();
        tm.addTask("low", 1, new ArrayList<>());
        tm.addTask("high", 3, new ArrayList<>());
        tm.addTask("medium", 2, new ArrayList<>());
        List<String> ordered = tm.sortByPriority().stream()
                .map(Task::getTitle)
                .collect(Collectors.toList());
        assertEquals(List.of("high", "medium", "low"), ordered,
                "sortByPriority() should order tasks by priority HIGHEST first (3 -> 2 -> 1), "
                        + "so the order should be high, medium, low");
    }

    // -----------------------------------------------------------------------
    // The 2 harder bugs (edge cases)
    // -----------------------------------------------------------------------

    @Test
    void tasksAddedWithoutTagsDoNotShareAList() {
        // Two tasks are created WITHOUT passing tags, so each should own a separate,
        // independent empty list. We then tag only task 'a' and check task 'b' is
        // untouched. If both tasks secretly share the same list object, tagging 'a' will
        // also tag 'b'.
        TaskManager tm = new TaskManager();
        Task a = tm.addTask("task a");
        Task b = tm.addTask("task b");
        a.addTag("urgent");
        assertEquals(List.of("urgent"), a.getTags(), "adding a tag to task a should update task a");
        assertTrue(b.getTags().isEmpty(),
                "each task added without tags must get its OWN empty tag list; task b should stay "
                        + "empty when task a is modified (if not, the two tasks share one list)");
    }

    @Test
    void idsAreNeverReusedAfterRemoval() {
        // Create 3 tasks (ids 1, 2, 3), then delete the middle one. When we add a 4th
        // task, its id must be a brand-new value (4) that has never been used. A
        // tempting-but-wrong id scheme derives the next id from how many tasks currently
        // exist, which after a removal would hand out an id that still belongs to another
        // task (a collision).
        TaskManager tm = new TaskManager();
        tm.addTask("a");            // id 1
        tm.addTask("b");            // id 2
        tm.addTask("c");            // id 3
        tm.removeTask(2);           // remove the middle one -> tasks are now ids 1 and 3
        Task d = tm.addTask("d");   // must NOT collide with the existing id 3
        assertEquals(4, d.getId(),
                "ids must never be reused: after adding 3 tasks the next id should be 4, even "
                        + "though one task was removed (id must not be derived from list size)");
    }

    // -----------------------------------------------------------------------
    // Correct helper (kept as a clean reference / used by the feature half)
    // -----------------------------------------------------------------------

    @Test
    void filterByTag() {
        // filterByTag returns every task carrying the given tag. Tasks 'a' and 'c' are
        // tagged "work"; 'b' is not, so it should be excluded. (This method is already
        // correct — it's a reference for how the others should behave.)
        TaskManager tm = new TaskManager();
        tm.addTask("a", 1, new ArrayList<>(List.of("work")));
        tm.addTask("b", 1, new ArrayList<>(List.of("home")));
        tm.addTask("c", 1, new ArrayList<>(List.of("work", "urgent")));
        List<String> titles = tm.filterByTag("work").stream()
                .map(Task::getTitle)
                .sorted()
                .collect(Collectors.toList());
        assertEquals(List.of("a", "c"), titles,
                "filterByTag('work') should return every task whose tags include 'work' (a and c)");
    }
}
