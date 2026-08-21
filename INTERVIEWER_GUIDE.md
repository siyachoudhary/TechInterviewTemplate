# Interviewer Guide (do NOT share with candidates)

This is the answer key and facilitation guide. Keep it out of the candidate's copy of the
repo (or hand them a branch/zip without this file).

## Timing

| Segment | Time | Notes |
|---------|------|-------|
| Intro + setup | 5 min | Make sure their env runs the tests. |
| Part 1: Debugging | ~30 min | 4 bugs. See below. |
| Part 2: Feature | ~25 min | Open-ended, tools allowed. |
| Wrap-up | ~5 min | Reflection / extension questions. |

Total ≈ 60 min. If setup drags, have them use the language they know best.

## The four planted bugs (in `manager.py` / `TaskManager.java`)

1. **Mutable/shared default tags.**
   - Python: `add_task(self, title, priority=1, tags=[])` — the default `[]` is created
     once and shared across every call that omits `tags`. Adding a tag to one task mutates
     the shared list, so it leaks into other tasks.
   - Java: `DEFAULT_TAGS` static list passed by reference from the `addTask(title)`
     overload — same shared-mutable-state bug.
   - **Fix:** create a fresh list per task (Python: `tags=None` then `tags = tags or []`;
     Java: pass `new ArrayList<>()`).
   - Caught by: `test_tags_are_independent` / `tagsAreIndependent`.

2. **`complete_task` uses the id as a list index.**
   - `self.tasks[task_id]` / `tasks.get(id)`. Ids start at 1, list indices at 0, so
     completing the first task (id 1) throws IndexError/IndexOutOfBounds.
   - **Fix:** look the task up by id (there's already a correct `get_task`/`getTask`
     helper to reuse).
   - Caught by: `test_complete_task_marks_completed` / `completeTaskMarksCompleted`.

3. **`get_pending` returns the wrong set (inverted condition).**
   - Returns tasks where `completed` is True instead of False.
   - **Fix:** invert the condition (`if not t.completed` / `if (!t.isCompleted())`).
   - Caught by: `test_get_pending_excludes_completed` / `getPendingExcludesCompleted`.

4. **`sort_by_priority` sorts ascending (low priority first).**
   - Should return highest priority first.
   - **Fix:** sort descending (Python: `reverse=True`; Java: `.reversed()` on the
     comparator).
   - Caught by: `test_sort_by_priority_high_first` / `sortByPriorityHighFirst`.

`filter_by_tag` / `filterByTag` and `get_task` / `getTask` are **correct** on purpose —
they're clean reference points and are used by the suggested features.

## What good looks like

- Reads the *test* first to learn intended behavior before diving into source.
- Fixes one bug, re-runs, confirms, moves on — rather than shotgun editing.
- Recognizes the mutable-default / shared-reference bug as a language footgun and can
  explain *why* it happens, not just paste the fix.
- Reuses `get_task`/`getTask` for bug #2 instead of writing a new loop.

## Feature part — evaluation

Any of the suggested features (or their own) is fine. Look for:
- A working implementation with at least one meaningful test.
- Edge-case awareness (empty results, case-insensitivity, cap behavior on priority bump,
  completed tasks excluded from overdue, etc.).
- Comfortable, sensible use of external tools/AI — can explain what they accepted and why.

## Sample follow-up questions

- Bug #1: "Why does this only bite when `tags` is omitted?"
- Bug #2: "How would a type checker or a test have caught this earlier?"
- Feature: "How would this change if tasks lived in a database instead of memory?"
