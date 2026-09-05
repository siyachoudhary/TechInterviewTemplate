# Interviewer Guide — TaskFlow (do NOT share with candidates)

This is the answer key and facilitation guide. Keep it out of the candidate's copy of the
repo (or hand them a branch/zip without this file).

## Timing

| Segment | Time | Notes |
|---------|------|-------|
| Intro + setup | 5 min | Make sure their env runs the tests. |
| Part 1: Debugging | ~45 min | 6 subtle bugs. This is the main event. |
| Part 2: Feature (**extra credit**) | only if they finish debugging with time to spare (rough bar: all green in < 30 min) | Open-ended, tools allowed. |
| Wrap-up | ~5 min | Reflection / extension questions. |

The bugs were deliberately made harder than a typical "spot the typo" set: **there are no
loud failures** (no stack traces, no `count() == -1`). Every bug is a plausible
implementation that quietly disagrees with the docstring. Expect a strong candidate
(e.g. someone with big-tech internship experience) to still need a real ~25–40 minutes and
to reason, not pattern-match. The feature half is a **bonus** — only reach for it if
they've cleared all six comfortably early. Don't let a candidate skimp on narration to race
into it.

Shipped state: **6 failed, 2 passed.** Fully fixed: **8 passed.**

## Wave 1 — a careful docstring read catches these

1. **`count` counts only *pending* tasks.** Returns the number of not-completed tasks
   instead of all tracked tasks, so completing a task makes the count drop.
   - Python: `return sum(1 for task in self.tasks if not task.completed)`
   - Java: loops and counts `if (!task.isCompleted())`.
   - **Fix:** count every task — `return len(self.tasks)` / `return tasks.size();`.
   - Caught by: `test_count_includes_completed_tasks` / `countIncludesCompletedTasks`.
   - Talking point: the docstring says "completed or not"; the code silently filters.

2. **`complete_task` toggles instead of setting completed.** It does
   `task.completed = not task.completed`, so completing an *already-completed* task flips it
   back to pending. The first completion looks correct; the bug only shows on the second
   call. (Note the lookup itself is correct — it uses `get_task`, so there's no crash.)
   - **Fix:** set it unconditionally — `task.completed = True` /
     `task.setCompleted(true)`.
   - Caught by: `test_complete_task_is_idempotent` / `completeTaskIsIdempotent`.
   - Talking point: idempotency — "what should completing something twice do?"

## Wave 2 — edge cases, ordering, aliasing, side effects

3. **`sort_by_priority` is not stable.** It sorts ascending and then reverses the whole
   list (`list(reversed(sorted(...)))` / `sort(...); Collections.reverse(...)`). That
   *does* put the highest priority first, but reversing flips the order of equal-priority
   tasks, so ties come out backwards.
   - **Fix:** sort descending directly, which is stable —
     `sorted(self.tasks, key=lambda t: t.priority, reverse=True)` /
     `sort(Comparator.comparingInt(Task::getPriority).reversed())`.
   - Caught by: `test_sort_by_priority_is_stable_high_first` / `sortByPriorityIsStableHighFirst`.
   - Why it's hard: the count and the top element look right; only the tie order is wrong,
     and the failing assertion is about two same-priority tasks swapping.

4. **`get_pending` mutates the manager.** It reassigns `self.tasks` to just the pending
   tasks (Java: `tasks.removeIf(Task::isCompleted)`) and returns that. The *returned* list
   is correct, so it looks fine — but it has **deleted the completed tasks** from the
   manager as a side effect.
   - **Fix:** build and return a new list without touching `self.tasks` —
     `return [task for task in self.tasks if not task.completed]` / build a fresh
     `ArrayList` and return it.
   - Caught by: `test_get_pending_is_non_destructive` / `getPendingIsNonDestructive`.
   - Why it's hard: the assertion that fails isn't about the returned value at all — it's
     that the completed task is gone afterwards (`get_task(1)` is now `None`).

5. **Shared mutable default tags.** Python: `add_task(self, title, priority=1, tags=[])` —
   the default `[]` is created **once** at definition time and shared across every call that
   omits `tags`, so all tag-less tasks point at the *same* list. Java: the `addTask(title)`
   overload passes the static `DEFAULT_TAGS` list by reference.
   - **Fix:** give each task its own list (Python: default `tags=None`, then
     `tags if tags is not None else []`; Java: pass `new ArrayList<>()`).
   - Caught by: `test_tasks_added_without_tags_do_not_share_a_list` /
     `tasksAddedWithoutTagsDoNotShareAList`.
   - Why it's hard: the buggy line looks totally normal, and the failing assertion is about
     task B even though task A was mutated. Ask "why only when `tags` is omitted?"

6. **Ids derived from list length, so they get reused after removal.** `_next_id` returns
   `len(self.tasks) + 1` / `nextId()` returns `tasks.size() + 1`. Fine while you only
   append, but after a removal the length drops and the next add reuses an id that still
   belongs to another task — a collision.
   - **Fix:** a monotonic counter that only ever increments (an `_id_counter` /
     `idCounter` field), independent of list size.
   - Caught by: `test_ids_are_never_reused_after_removal` / `idsAreNeverReusedAfterRemoval`.
   - Why it's hard: every basic add/list test passes; only add → remove → add exposes it.

`filter_by_tag` / `filterByTag` and `get_task` / `getTask` are **correct** on purpose
(clean reference points; the two passing tests exercise the add happy-path and
`filter_by_tag`). `remove_task` / `removeTask` is also correct and is used by bug #6's test.

## What good looks like

- Reads the *test* and the method *docstring* first to learn intended behavior before
  editing.
- Fixes one bug, re-runs, confirms, moves on — rather than shotgun editing.
- On #4, notices the returned value "looks right" and checks for a **side effect** instead
  of stopping at the return value.
- On #3, names *stability* as the property being violated, not just "sort is backwards."
- On #2, reasons about **idempotency** rather than only making the one assertion pass.
- On #5, recognizes the language footgun and can explain *why* it happens.
- On #6, connects "id derived from length" to "reuse after removal."

## Feature part (extra credit only) — evaluation

Only reached if debugging finished early. The README ships a **worked example**
(`search(keyword)`) so candidates know the expected level of finish: a small clean method
*plus* a test that pins the edge cases. Look for:
- A working implementation with at least one meaningful test.
- Edge-case awareness (empty input, case-insensitivity, cap behavior on priority bump,
  completed tasks excluded from overdue, etc.).
- Comfortable, sensible use of external tools/AI — can explain what they accepted and why.

## Sample follow-up questions

- Bug #2: "What should completing an already-completed task do? What's that property
  called?"
- Bug #3: "Your fix put the top item first — how do you know it didn't scramble ties?"
- Bug #4: "The returned list was correct — so what actually broke, and how would a test
  catch a read-only-query violation in general?"
- Bug #5: "Why does this only bite when `tags` is omitted?"
- Bug #6: "What other operation besides removal would expose this — or is the id scheme
  itself wrong?"
- Feature: "How would this change if tasks lived in a database instead of memory?"
