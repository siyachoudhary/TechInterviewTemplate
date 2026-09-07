# TaskFlow — Debugging Technical Interview

Welcome! This is a **timeboxed (~60 minute)** technical interview built around a small
task-management library called **TaskFlow**. There are two identical implementations in
the same repo — **Python** and **Java** — so pick whichever language you're most
comfortable in.

The interview is really **one main task with an optional bonus**:

1. **Debugging (the whole interview)** — The library ships with a failing test suite. Eight
   bugs have been planted. Your job is to find and fix them until the tests are green. None
   of them are one-liners that scream at you — they're the kind of plausible-looking code
   that quietly does the wrong thing, so take your time and reason carefully.
2. **Add a Feature (extra credit)** — *Only if you finish the debugging comfortably early*
   (roughly, all tests green in under 30 minutes) we'll spend the remaining time adding a
   small feature together. This is a bonus, not a requirement — a thorough, well-narrated
   debugging pass is the main thing we're evaluating.

We're not looking for perfection. We want to see how you read unfamiliar code, form
hypotheses, verify them, and communicate as you go. **Think out loud.**

---

## What is TaskFlow?

A tiny in-memory task tracker. Tasks have an `id`, `title`, `priority` (1 = low,
2 = medium, 3 = high), a list of `tags`, and a `completed` flag. The `TaskManager` class
lets you add tasks, look them up, complete them, remove them, count them, filter by tag,
list pending tasks, sort by priority, clear out the completed ones, and report the average
priority.

The two language implementations behave identically — same classes, same methods, same bugs.

---

## Setup

Clone the repo and pick a language.

### Prerequisites

| Language | Needs |
|----------|-------|
| Python   | Python 3.9+ and `pytest` (`pip install pytest`) |
| Java     | JDK 17+ and Maven 3.8+ |

### Python

```bash
cd python
python -m pip install pytest        # once
python -m pytest -v                 # run the tests
```

### Java

```bash
cd java
mvn test                            # compiles and runs the tests
```

> Tip: run a single test while iterating.
> - Python: `python -m pytest tests/test_manager.py::test_complete_task_marks_completed -v`
> - Java: `mvn -Dtest=TaskManagerTest#completeTaskMarksCompleted test`

---

## Part 1 — Debugging (the main task)

1. Run the test suite. You should see multiple failures.
2. Read the failing tests in `tests/` (Python) or `src/test/` (Java) to understand the
   *intended* behavior.
3. Open the source (`taskmanager/manager.py` or
   `src/main/java/com/example/taskmanager/`) and fix the bugs.
4. Re-run until everything is green.

There are **eight** planted bugs, and **none of them are loud** — there are no crashes or
wildly-wrong values to point the way. Each is a plausible implementation that quietly
disagrees with the method's docstring: an off-by-one in the wrong direction, a query that
mutates state it shouldn't, a sort that isn't stable, shared/aliased data, an
identity/id scheme that breaks under mutation, a loop that mutates the list it is walking, a
truncated average, and so on. The **docstring on each method states what it is supposed to
do** — the bug is (almost) always a mismatch between that description and the code.

The test file groups the bugs into two waves: *Wave 1* is catchable from a careful read of
the docstring; *Wave 2* only bites on an edge case (ordering, aliasing, a side effect, an
adjacent pair removed in one pass, a truncated average, or a sequence of operations), so the
failing assertion may name a value the buggy line never touches. Note that not every method
is broken, and one buggy method can still look correct on a friendly input. Fix the source,
**not** the tests — the tests describe correct behavior.

**As you work, tell us:** what does the failing test expect, what did you observe, what's
your hypothesis, and how did the fix confirm it?

---

## Part 2 — Add a Feature (extra credit — only if you finish early)

**This part is a bonus.** We only reach it if you've finished the debugging comfortably
early — as a rough rule of thumb, all tests green in **under 30 minutes** with time to
spare. If debugging takes the whole session, that's completely fine; a careful, well-
narrated debugging pass is what we're really evaluating. Don't rush Part 1 to get here.

If we do have time: pick **one** feature below (or propose your own) and implement it,
**including at least one test**. This half is intentionally open — reach for whatever tools
and references you'd normally use (docs, Google, StackOverflow, AI assistants such as
Copilot/ChatGPT/Claude, etc.). We care about how you approach the problem, not whether you
memorized an API.

Suggested features (in rough order of scope):

- **Keyword search.** Add `search(keyword)` that returns tasks whose title contains the
  keyword, case-insensitively.
- **Tag summary.** Add `tag_counts()` / `tagCounts()` returning a map of tag → number of
  tasks with that tag.
- **Priority bump.** Add `bump_priority(id)` / `bumpPriority(id)` that raises a task's
  priority by one level (capped at high), and decide what should happen at the cap.
- **Due dates & overdue list.** Add an optional due date to tasks and a
  `get_overdue(today)` / `getOverdue(today)` method returning uncompleted tasks past due.

Walk us through your design choices, edge cases, and how you'd extend it further with more
time.

### Worked example: adding `search(keyword)`

To make the expectations concrete, here's exactly what a good "extra credit" answer to the
**keyword search** feature looks like end-to-end. (You don't have to pick this one — it's
just a reference for the level of finish we're after: a small, clean method *plus* a test
that pins down the interesting edge cases.)

**1. Add the method to `TaskManager`.** Reuse the existing style; think about the edge
cases (case-insensitivity, no matches):

```python
# taskmanager/manager.py
def search(self, keyword):
    """Return every task whose title contains `keyword`, case-insensitively.

    An empty keyword matches nothing; matching is a substring test, not whole-word.
    """
    if not keyword:
        return []
    needle = keyword.lower()
    return [task for task in self.tasks if needle in task.title.lower()]
```

```java
// TaskManager.java
/** Return every task whose title contains `keyword`, case-insensitively. */
public List<Task> search(String keyword) {
    List<Task> matches = new ArrayList<>();
    if (keyword == null || keyword.isEmpty()) {
        return matches;
    }
    String needle = keyword.toLowerCase();
    for (Task task : tasks) {
        if (task.getTitle().toLowerCase().contains(needle)) {
            matches.add(task);
        }
    }
    return matches;
}
```

**2. Add a test that proves it — including the edge cases**, not just the happy path:

```python
# tests/test_manager.py
def test_search_is_case_insensitive_substring_match():
    tm = TaskManager()
    tm.add_task("Write REPORT")
    tm.add_task("review PR")
    tm.add_task("report to manager")
    titles = sorted(t.title for t in tm.search("report"))
    assert titles == ["Write REPORT", "report to manager"], "search matches case-insensitively, anywhere in the title"
    assert tm.search("") == [], "an empty keyword matches nothing"
    assert tm.search("xyz") == [], "a keyword with no matches returns an empty list"
```

**3. Talk us through it:** why case-insensitive, why an empty keyword returns nothing
rather than everything, and how you'd extend it (search tags too? rank by relevance? a
`limit`?). That narration — the edge cases you *chose* to handle and why — is the point of
the exercise, not the line count.

---

## What we're evaluating

- **Debugging method** — reading code, isolating faults, verifying fixes.
- **Communication** — narrating your reasoning and trade-offs.
- **Code quality** — clean, readable changes that match the surrounding style.
- **Feature judgment** — sensible design, edge-case awareness, and a test that proves it.

Good luck — and remember to think out loud!
