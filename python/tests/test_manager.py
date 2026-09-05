"""Behavioral tests for TaskFlow.

These describe the *intended* behavior. Fix the source in taskmanager/manager.py
until they all pass — do not change the tests.

There are 6 planted bugs. None of them announce themselves with a crash or an obviously
absurd value — every one is a plausible-looking implementation that quietly disagrees with
the docstring. Read the method's docstring (it states the intended behavior), then read the
code, and find the mismatch. The tests are grouped in two waves:

  * Wave 1 — a careful read of the docstring is enough to spot the mismatch.
  * Wave 2 — the bug only shows on an edge case (ordering, aliasing, a side effect, or a
    sequence of operations), so the failing assertion may be about a value the buggy line
    never names.

Each assertion carries a message describing the intended behavior.
"""

import pytest

from taskmanager import TaskManager


# ---------------------------------------------------------------------------
# Wave 1 — read the docstring carefully
# ---------------------------------------------------------------------------

def test_count_includes_completed_tasks():
    # count() reports how many tasks are TRACKED, regardless of whether they are done.
    # Completing a task doesn't make it stop existing, so the count must not drop.
    tm = TaskManager()
    assert tm.count() == 0, "count() on an empty manager should be 0"
    tm.add_task("a")
    tm.add_task("b")
    tm.get_task(1).completed = True   # mark one done directly (no reliance on complete_task)
    assert tm.count() == 2, (
        "count() should report every tracked task, completed or not (2 here); completing a "
        "task must not remove it from the count"
    )


def test_complete_task_is_idempotent():
    # Completing a task marks it done. Completing it AGAIN must leave it done — the flag is
    # set to True, not flipped.
    tm = TaskManager()
    tm.add_task("write report")
    first = tm.complete_task(1)
    assert first.completed is True, (
        "complete_task(id) should find the task with that id, mark it completed, and return it"
    )
    tm.complete_task(1)   # completing an already-completed task
    assert tm.get_task(1).completed is True, (
        "complete_task should be idempotent: completing an already-completed task leaves it "
        "completed, it must not toggle back to pending"
    )


# ---------------------------------------------------------------------------
# Wave 2 — edge cases, ordering, aliasing, and side effects
# ---------------------------------------------------------------------------

def test_sort_by_priority_is_stable_high_first():
    # sort_by_priority returns highest priority first. Among tasks that TIE on priority, the
    # original insertion order must be preserved (a stable sort): high-1 was added before
    # high-2, so high-1 must come first.
    tm = TaskManager()
    tm.add_task("high-1", priority=3)
    tm.add_task("high-2", priority=3)
    tm.add_task("low", priority=1)
    ordered = [t.title for t in tm.sort_by_priority()]
    assert ordered == ["high-1", "high-2", "low"], (
        "sort_by_priority() should order by priority highest-first AND keep insertion order "
        "among ties, so ['high-1', 'high-2', 'low'] — not a reversal that flips the two "
        "equal-priority tasks"
    )


def test_get_pending_is_non_destructive():
    # get_pending is a read-only query. Asking for the pending tasks must NOT delete the
    # completed ones from the manager — the returned list looks right either way, so the
    # tell is whether the completed task is still there afterwards.
    tm = TaskManager()
    tm.add_task("a")
    tm.add_task("b")
    tm.get_task(1).completed = True
    pending = [t.title for t in tm.get_pending()]
    assert pending == ["b"], (
        "get_pending() should return only not-completed tasks; 'a' was completed, so only "
        "'b' should come back"
    )
    assert tm.get_task(1) is not None, (
        "get_pending() must not modify the stored tasks; task 'a' was only completed, not "
        "removed, so it should still be retrievable afterwards"
    )
    assert tm.get_task(2) is not None, (
        "the pending task 'b' must also still be tracked after calling get_pending()"
    )


def test_tasks_added_without_tags_do_not_share_a_list():
    """Adding a tag to one task must not affect another task added without tags."""
    # Two tasks are created WITHOUT passing tags, so each should own a separate, independent
    # empty list. We then tag only task 'a' and check task 'b' is untouched. If both tasks
    # secretly share the same list object, tagging 'a' will also tag 'b'.
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
# Correct helpers (these pass out of the box — clean reference points)
# ---------------------------------------------------------------------------

def test_add_task_returns_task_with_incrementing_ids():
    # Sanity check on the happy path: adding tasks assigns sequential ids (1, 2, ...) and
    # remembers the title. This one should pass out of the box.
    tm = TaskManager()
    a = tm.add_task("write report")
    b = tm.add_task("review PR")
    assert a.id == 1, "the first task added should get id 1"
    assert b.id == 2, "the second task added should get id 2 (ids increment by 1)"
    assert a.title == "write report", "add_task should store the title it was given"


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
