"""Behavioral tests for TaskFlow.

These describe the *intended* behavior. Fix the source in taskmanager/manager.py
until they all pass — do not change the tests.

There are 6 planted bugs: 4 are easy to spot from a single failing test, and 2 are
subtler (they only bite on an edge case). The tests are grouped accordingly.
"""

import pytest

from taskmanager import TaskManager


# ---------------------------------------------------------------------------
# The 4 easier bugs
# ---------------------------------------------------------------------------

def test_add_task_returns_task_with_incrementing_ids():
    tm = TaskManager()
    a = tm.add_task("write report")
    b = tm.add_task("review PR")
    assert a.id == 1
    assert b.id == 2
    assert a.title == "write report"


def test_count_reflects_number_of_tasks():
    tm = TaskManager()
    assert tm.count() == 0
    tm.add_task("a")
    tm.add_task("b")
    assert tm.count() == 2


def test_complete_task_marks_completed():
    tm = TaskManager()
    tm.add_task("write report")
    task = tm.complete_task(1)
    assert task.completed is True
    assert tm.get_task(1).completed is True


def test_get_pending_excludes_completed():
    tm = TaskManager()
    tm.add_task("a")
    tm.add_task("b")
    tm.get_task(1).completed = True
    pending_titles = [t.title for t in tm.get_pending()]
    assert pending_titles == ["b"]


def test_sort_by_priority_high_first():
    tm = TaskManager()
    tm.add_task("low", priority=1)
    tm.add_task("high", priority=3)
    tm.add_task("medium", priority=2)
    ordered = [t.title for t in tm.sort_by_priority()]
    assert ordered == ["high", "medium", "low"]


# ---------------------------------------------------------------------------
# The 2 harder bugs (edge cases)
# ---------------------------------------------------------------------------

def test_tasks_added_without_tags_do_not_share_a_list():
    """Adding a tag to one task must not affect another task added without tags."""
    tm = TaskManager()
    a = tm.add_task("task a")
    b = tm.add_task("task b")
    a.tags.append("urgent")
    assert a.tags == ["urgent"]
    assert b.tags == []


def test_ids_are_never_reused_after_removal():
    """After removing a task, a newly added task must get a fresh, unique id."""
    tm = TaskManager()
    tm.add_task("a")            # id 1
    tm.add_task("b")            # id 2
    tm.add_task("c")            # id 3
    tm.remove_task(2)           # remove the middle one
    d = tm.add_task("d")        # must NOT collide with the existing id 3
    existing_ids = [t.id for t in tm.tasks]
    assert d.id == 4
    assert len(existing_ids) == len(set(existing_ids)), "ids must be unique"


# ---------------------------------------------------------------------------
# Correct helpers (kept as clean reference points / used by the feature half)
# ---------------------------------------------------------------------------

def test_filter_by_tag():
    tm = TaskManager()
    tm.add_task("a", tags=["work"])
    tm.add_task("b", tags=["home"])
    tm.add_task("c", tags=["work", "urgent"])
    titles = sorted(t.title for t in tm.filter_by_tag("work"))
    assert titles == ["a", "c"]
