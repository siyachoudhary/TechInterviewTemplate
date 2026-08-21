"""Behavioral tests for TaskFlow.

These describe the *intended* behavior. Fix the source in taskmanager/manager.py
until they all pass — do not change the tests.

There are 6 planted bugs: 4 are easy to spot from a single failing test, and 2 are
subtler (they only bite on an edge case). The tests are grouped accordingly.

Each assertion carries a message describing the intended behavior, so a failure tells
you what the method is *supposed* to do — not just how two values differ.
"""

import pytest

from taskmanager import TaskManager


# ---------------------------------------------------------------------------
# The 4 easier bugs
# ---------------------------------------------------------------------------

def test_add_task_returns_task_with_incrementing_ids():
    # Sanity check on the happy path: adding tasks assigns sequential ids (1, 2, ...)
    # and remembers the title. This one should pass out of the box.
    tm = TaskManager()
    a = tm.add_task("write report")
    b = tm.add_task("review PR")
    assert a.id == 1, "the first task added should get id 1"
    assert b.id == 2, "the second task added should get id 2 (ids increment by 1)"
    assert a.title == "write report", "add_task should store the title it was given"


def test_count_reflects_number_of_tasks():
    # count() should simply report how many tasks are tracked: 0 when empty,
    # then 2 after adding two.
    tm = TaskManager()
    assert tm.count() == 0, "count() on an empty manager should be 0"
    tm.add_task("a")
    tm.add_task("b")
    assert tm.count() == 2, "count() should equal the number of tasks added (2 here)"


def test_complete_task_marks_completed():
    # Add one task (it gets id 1), then complete it BY ITS ID. Completing task id 1
    # should flip its `completed` flag to True and that change should stick.
    tm = TaskManager()
    tm.add_task("write report")
    task = tm.complete_task(1)
    assert task.completed is True, (
        "complete_task(id) should find the task with that id and mark it completed, "
        "then return it"
    )
    assert tm.get_task(1).completed is True, (
        "the completion must persist on the stored task, not just the returned copy"
    )


def test_get_pending_excludes_completed():
    # Add two tasks, mark the first ('a') completed directly, then ask for pending work.
    # "Pending" means not-yet-completed, so only 'b' should come back.
    tm = TaskManager()
    tm.add_task("a")
    tm.add_task("b")
    tm.get_task(1).completed = True
    pending_titles = [t.title for t in tm.get_pending()]
    assert pending_titles == ["b"], (
        "get_pending() should return only NOT-completed tasks; task 'a' was completed, "
        "so only 'b' should remain"
    )


def test_sort_by_priority_high_first():
    # Add tasks out of priority order. sort_by_priority() should return them ordered
    # by priority with the HIGHEST (3) first, so: high, medium, low.
    tm = TaskManager()
    tm.add_task("low", priority=1)
    tm.add_task("high", priority=3)
    tm.add_task("medium", priority=2)
    ordered = [t.title for t in tm.sort_by_priority()]
    assert ordered == ["high", "medium", "low"], (
        "sort_by_priority() should order tasks by priority HIGHEST first "
        "(3 -> 2 -> 1), so the order should be high, medium, low"
    )


# ---------------------------------------------------------------------------
# The 2 harder bugs (edge cases)
# ---------------------------------------------------------------------------

def test_tasks_added_without_tags_do_not_share_a_list():
    """Adding a tag to one task must not affect another task added without tags."""
    # Two tasks are created WITHOUT passing tags, so each should own a separate,
    # independent empty list. We then tag only task 'a' and check task 'b' is untouched.
    # If both tasks secretly share the same list object, tagging 'a' will also tag 'b'.
    tm = TaskManager()
    a = tm.add_task("task a")
    b = tm.add_task("task b")
    a.tags.append("urgent")
    assert a.tags == ["urgent"], "appending to task a's tags should update task a"
    assert b.tags == [], (
        "each task added without tags must get its OWN empty tag list; task b should "
        "stay empty when task a is modified (if it doesn't, the two tasks are sharing "
        "one list object)"
    )


def test_ids_are_never_reused_after_removal():
    """After removing a task, a newly added task must get a fresh, unique id."""
    # Create 3 tasks (ids 1, 2, 3), then delete the middle one. When we add a 4th task,
    # its id must be a brand-new value (4) that has never been used. A tempting-but-wrong
    # id scheme derives the next id from how many tasks currently exist, which after a
    # removal would hand out an id that still belongs to another task (a collision).
    tm = TaskManager()
    tm.add_task("a")            # id 1
    tm.add_task("b")            # id 2
    tm.add_task("c")            # id 3
    tm.remove_task(2)           # remove the middle one -> tasks are now ids 1 and 3
    d = tm.add_task("d")        # must NOT collide with the existing id 3
    existing_ids = [t.id for t in tm.tasks]
    assert d.id == 4, (
        "ids must never be reused: after adding 3 tasks the next id should be 4, even "
        "though one task was removed (id must not be derived from the current list size)"
    )
    assert len(existing_ids) == len(set(existing_ids)), (
        f"every task must have a unique id, but got duplicates: {existing_ids}"
    )


# ---------------------------------------------------------------------------
# Correct helpers (kept as clean reference points / used by the feature half)
# ---------------------------------------------------------------------------

def test_filter_by_tag():
    # filter_by_tag returns every task carrying the given tag. Tasks 'a' and 'c' are
    # tagged "work"; 'b' is not, so it should be excluded. (This method is already
    # correct — it's a reference for how the others should behave.)
    tm = TaskManager()
    tm.add_task("a", tags=["work"])
    tm.add_task("b", tags=["home"])
    tm.add_task("c", tags=["work", "urgent"])
    titles = sorted(t.title for t in tm.filter_by_tag("work"))
    assert titles == ["a", "c"], (
        "filter_by_tag('work') should return every task whose tags include 'work' "
        "(tasks a and c)"
    )
