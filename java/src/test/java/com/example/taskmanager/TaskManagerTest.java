package com.example.taskmanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Behavioral tests for TaskFlow. These describe the *intended* behavior.
 * Fix the source in TaskManager.java until they all pass — do not change the tests.
 *
 * There are 8 planted bugs. None of them announce themselves with a crash or an obviously
 * absurd value — every one is a plausible-looking implementation that quietly disagrees with
 * the Javadoc. Read the method's Javadoc (it states the intended behavior), then read the
 * code, and find the mismatch. The tests come in two waves:
 *
 *   - Wave 1: a careful read of the Javadoc is enough to spot the mismatch.
 *   - Wave 2: the bug only shows on an edge case (ordering, aliasing, a side effect, an
 *     adjacent pair removed in one pass, a truncated average, or a sequence of operations),
 *     so the failing assertion may be about a value the buggy line never names. Note: not
 *     every method is broken, and one buggy method can still look fine on a friendly input.
 *
 * Each assertion carries a message describing the intended behavior.
 */
class TaskManagerTest {

    // -----------------------------------------------------------------------
    // Wave 1 — read the Javadoc carefully
    // -----------------------------------------------------------------------

    @Test
    void countIncludesCompletedTasks() {
        // count() reports how many tasks are TRACKED, regardless of whether they are done.
        // Completing a task doesn't make it stop existing, so the count must not drop.
        TaskManager tm = new TaskManager();
        assertEquals(0, tm.count(), "count() on an empty manager should be 0");
        tm.addTask("a");
        tm.addTask("b");
        tm.getTask(1).setCompleted(true);   // mark one done directly (not via completeTask)
        assertEquals(2, tm.count(),
                "count() should report every tracked task, completed or not (2 here); completing "
                        + "a task must not remove it from the count");
    }

    @Test
    void completeTaskIsIdempotent() {
        // Completing a task marks it done. Completing it AGAIN must leave it done — the flag
        // is set to true, not flipped.
        TaskManager tm = new TaskManager();
        tm.addTask("write report");
        Task first = tm.completeTask(1);
        assertTrue(first.isCompleted(),
                "completeTask(id) should find the task with that id, mark it completed, and return it");
        tm.completeTask(1);   // completing an already-completed task
        assertTrue(tm.getTask(1).isCompleted(),
                "completeTask should be idempotent: completing an already-completed task leaves it "
                        + "completed, it must not toggle back to pending");
    }

    // -----------------------------------------------------------------------
    // Wave 2 — edge cases, ordering, aliasing, and side effects
    // -----------------------------------------------------------------------

    @Test
    void sortByPriorityIsStableHighFirst() {
        // sortByPriority returns highest priority first. Among tasks that TIE on priority, the
        // original insertion order must be preserved (a stable sort): high-1 was added before
        // high-2, so high-1 must come first.
        TaskManager tm = new TaskManager();
        tm.addTask("high-1", 3, new ArrayList<>());
        tm.addTask("high-2", 3, new ArrayList<>());
        tm.addTask("low", 1, new ArrayList<>());
        List<String> ordered = tm.sortByPriority().stream()
                .map(Task::getTitle)
                .collect(Collectors.toList());
        assertEquals(List.of("high-1", "high-2", "low"), ordered,
                "sortByPriority() should order by priority highest-first AND keep insertion order "
                        + "among ties, so [high-1, high-2, low] — not a reversal that flips the two "
                        + "equal-priority tasks");
    }

    @Test
    void getPendingIsNonDestructive() {
        // getPending is a read-only query. Asking for the pending tasks must NOT delete the
        // completed ones from the manager — the returned list looks right either way, so the
        // tell is whether the completed task is still there afterwards.
        TaskManager tm = new TaskManager();
        tm.addTask("a");
        tm.addTask("b");
        tm.getTask(1).setCompleted(true);
        List<String> pending = tm.getPending().stream()
                .map(Task::getTitle)
                .collect(Collectors.toList());
        assertEquals(List.of("b"), pending,
                "getPending() should return only not-completed tasks; 'a' was completed, so only "
                        + "'b' should come back");
        assertNotNull(tm.getTask(1),
                "getPending() must not modify the stored tasks; task 'a' was only completed, not "
                        + "removed, so it should still be retrievable afterwards");
        assertNotNull(tm.getTask(2),
                "the pending task 'b' must also still be tracked after calling getPending()");
    }

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

    @Test
    void removeCompletedDropsEveryDoneTask() {
        // removeCompleted removes EVERY completed task. NOTE: the order they are added in is
        // load-bearing — keep the two completed tasks adjacent.
        TaskManager tm = new TaskManager();
        tm.addTask("a");
        tm.addTask("b");
        tm.addTask("c");
        tm.getTask(1).setCompleted(true);   // 'a' done
        tm.getTask(2).setCompleted(true);   // 'b' done, right after 'a'
        tm.removeCompleted();
        List<String> titles = tm.getTasks().stream()
                .map(Task::getTitle)
                .sorted()
                .collect(Collectors.toList());
        assertEquals(List.of("c"), titles,
                "removeCompleted() should drop EVERY completed task (both 'a' and 'b'), leaving only 'c'");
    }

    @Test
    void averagePriorityKeepsTheFraction() {
        // The mean priority is an exact value: priorities 3 and 2 average to 2.5, not 2. An
        // average computed with integer division truncates the fraction.
        TaskManager tm = new TaskManager();
        tm.addTask("high", 3, new ArrayList<>());
        tm.addTask("medium", 2, new ArrayList<>());
        assertEquals(2.5, tm.averagePriority(), 1e-9,
                "averagePriority() of priorities 3 and 2 is exactly (3 + 2) / 2.0 = 2.5; integer "
                        + "division would truncate this to 2");
    }

    @Test
    void averagePriorityOfUniformTasks() {
        // A friendly input for the same method: when the mean lands on a whole number, even a
        // truncating implementation looks correct. This one passes out of the box — it is NOT
        // proof that averagePriority() is right (see averagePriorityKeepsTheFraction).
        TaskManager tm = new TaskManager();
        tm.addTask("a", 2, new ArrayList<>());
        tm.addTask("b", 2, new ArrayList<>());
        assertEquals(2.0, tm.averagePriority(), 1e-9, "the mean of 2 and 2 is 2");
    }

    // -----------------------------------------------------------------------
    // Correct helpers (these pass out of the box — clean reference points)
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
