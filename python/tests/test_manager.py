"""Behavioral tests for TaskFlow.

These describe the *intended* behavior. Fix the source in taskmanager/manager.py
until they all pass — do not change the tests.
"""

import pytest

from taskmanager import TaskManager


def test_add_task_returns_task_with_incrementing_ids():
    tm = TaskManager()
    a = tm.add_task("write report")
    b = tm.add_task("review PR")
    assert a.id == 1
    assert b.id == 2
    assert a.title == "write report"


def test_tags_are_independent():
    """Two tasks added without tags must not share the same tag list."""
    tm = TaskManager()
    a = tm.add_task("task a")
    b = tm.add_task("task b")
    a.tags.append("urgent")
    assert a.tags == ["urgent"]
    assert b.tags == []


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


def test_filter_by_tag():
    tm = TaskManager()
    tm.add_task("a", tags=["work"])
    tm.add_task("b", tags=["home"])
    tm.add_task("c", tags=["work", "urgent"])
    titles = sorted(t.title for t in tm.filter_by_tag("work"))
    assert titles == ["a", "c"]
