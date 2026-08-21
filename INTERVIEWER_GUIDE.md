# Interviewer Guide (do NOT share with candidates)

This is the answer key and facilitation guide. Keep it out of the candidate's copy of the
repo (or hand them a branch/zip without this file).

## Timing

| Segment | Time | Notes |
|---------|------|-------|
| Intro + setup | 5 min | Make sure their env runs the tests. |
| Part 1: Debugging | ~35 min | 6 bugs: 4 easy + 2 hard. See below. |
| Part 2: Feature | ~20 min | Open-ended, tools allowed. |
| Wrap-up | ~5 min | Reflection / extension questions. |

Total ≈ 60 min. If setup drags, have them use the language they know best. A strong
candidate clears the 4 easy bugs quickly; the 2 hard ones are the real signal. It's fine
if a candidate doesn't finish both hard bugs — watch *how* they hunt.

## The 4 EASY bugs (loud failures — the test points near the cause)

1. **`count` is off by one.**
   - Returns `len(self.tasks) - 1` / `tasks.size() - 1`.
   - **Fix:** drop the `- 1`.
   - Caught by: `test_count_reflects_number_of_tasks` / `countReflectsNumberOfTasks`.

2. **`complete_task` uses the id as a list index.**
   - `self.tasks[task_id]` / `tasks.get(id)`. Ids start at 1, list indices at 0, so
     completing the first task (id 1) throws IndexError/IndexOutOfBounds — a stack trace
     pointing right at the line.
   - **Fix:** look the task up by id (reuse the correct `get_task`/`getTask` helper).
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

## The 2 HARD bugs (subtle — only bite on an edge case)

5. **Shared mutable default tags.**
   - Python: `add_task(self, title, priority=1, tags=[])` — the default `[]` is created
     **once** at function-definition time and shared across every call that omits `tags`.
     Every tag-less task ends up pointing at the *same* list, so mutating one task's tags
     silently mutates the others'. The happy-path tests pass; it only shows up when two
     tasks are added without tags and one is mutated.
   - Java: the `addTask(title)` overload passes the static `DEFAULT_TAGS` list by
     reference — same shared-mutable-state bug.
   - Why it's hard: the buggy line *looks* completely normal; the failing test is about
     task B even though the mutation happened to task A.
   - **Fix:** give each task its own list (Python: default `tags=None`, then
     `tags if tags is not None else []`; Java: pass `new ArrayList<>()`).
   - Caught by: `test_tasks_added_without_tags_do_not_share_a_list` /
     `tasksAddedWithoutTagsDoNotShareAList`.

6. **Ids are generated from the current list length, so they get reused after removal.**
   - `_next_id` returns `len(self.tasks) + 1` / `nextId()` returns `tasks.size() + 1`.
     Fine while you only ever append (1, 2, 3, …), but after `remove_task`/`removeTask`
     the length drops, so the next `add` reuses an id that still belongs to another task —
     an id collision that makes `get_task` return the wrong object.
   - Why it's hard: every basic add/list test passes; it only breaks in the
     add → remove → add sequence.
   - **Fix:** track a monotonic counter that only ever increments (e.g. an `_id_counter`
     field that never decreases), independent of the list size.
   - Caught by: `test_ids_are_never_reused_after_removal` / `idsAreNeverReusedAfterRemoval`.

`filter_by_tag` / `filterByTag`, `get_task` / `getTask`, and `remove_task` / `removeTask`
are **correct** on purpose — clean reference points, and used by the suggested features.

## What good looks like

- Reads the *test* first to learn intended behavior before diving into source.
- Fixes one bug, re-runs, confirms, moves on — rather than shotgun editing.
- Clears the 4 easy bugs efficiently, then slows down and reasons carefully about the 2
  hard ones instead of guessing.
- Recognizes the shared-mutable-default bug (#5) as a language footgun and can explain
  *why* it happens, not just paste the fix.
- For bug #6, connects "id derived from length" to "reuse after removal" — ideally
  reproduces the collision deliberately before fixing.
- Reuses `get_task`/`getTask` for bug #2 instead of writing a new loop.

## Feature part — evaluation

Any of the suggested features (or their own) is fine. Look for:
- A working implementation with at least one meaningful test.
- Edge-case awareness (empty results, case-insensitivity, cap behavior on priority bump,
  completed tasks excluded from overdue, etc.).
- Comfortable, sensible use of external tools/AI — can explain what they accepted and why.

## Sample follow-up questions

- Bug #5: "Why does this only bite when `tags` is omitted?"
- Bug #6: "What other operation besides removal would expose this? Would sorting or
  re-indexing help, or is the id scheme itself wrong?"
- Bug #2: "How would a type checker or a test have caught this earlier?"
- Feature: "How would this change if tasks lived in a database instead of memory?"
