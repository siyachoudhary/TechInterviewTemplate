# TaskFlow — Debugging Technical Interview

Welcome! This is a **timeboxed (~60 minute)** technical interview built around a small
task-management library called **TaskFlow**. There are two identical implementations in
the same repo — **Python** and **Java** — so pick whichever language you're most
comfortable in.

The interview has two parts:

1. **Debugging (~35 min)** — The library ships with a failing test suite. Six bugs have
   been planted (four easy, two subtle). Your job is to find and fix them until the tests
   are green.
2. **Feature (~20 min)** — Once tests pass, you'll add a new feature. This part is
   open-ended: use any external resources you like (docs, Google, StackOverflow, AI
   assistants such as Copilot/ChatGPT/Claude, etc.). We care about how you approach the
   problem, not whether you memorized an API.

We're not looking for perfection. We want to see how you read unfamiliar code, form
hypotheses, verify them, and communicate as you go. **Think out loud.**

---

## What is TaskFlow?

A tiny in-memory task tracker. Tasks have an `id`, `title`, `priority` (1 = low,
2 = medium, 3 = high), a list of `tags`, and a `completed` flag. The `TaskManager` class
lets you add tasks, look them up, complete them, remove them, count them, filter by tag,
list pending tasks, and sort by priority.

The two implementations behave identically — same classes, same methods, same bugs.

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

## Part 1 — Debugging (~35 min)

1. Run the test suite. You should see multiple failures.
2. Read the failing tests in `tests/` (Python) or `src/test/` (Java) to understand the
   *intended* behavior.
3. Open the source (`taskmanager/manager.py` or
   `src/main/java/com/example/taskmanager/`) and fix the bugs.
4. Re-run until everything is green.

There are **six** planted bugs: **four are easy to spot** from a single failing test
(a loud exception or an obviously-wrong result), and **two are subtler** — they only
surface on an edge case, so the failing test won't point straight at the buggy line. The
test file groups them into "easier" and "harder" sections to help you pace yourself. Fix
the source, **not** the tests — the tests describe correct behavior.

**As you work, tell us:** what does the failing test expect, what did you observe, what's
your hypothesis, and how did the fix confirm it?

---

## Part 2 — Add a Feature (~20 min)

Once the suite is green, pick **one** feature below (or propose your own) and implement
it, **including at least one test**. This half is intentionally open — reach for whatever
tools and references you'd normally use.

Suggested features (in rough order of scope):

- **Due dates & overdue list.** Add an optional due date to tasks and a
  `get_overdue(today)` / `getOverdue(today)` method returning uncompleted tasks past due.
- **Keyword search.** Add `search(keyword)` that returns tasks whose title contains the
  keyword, case-insensitively.
- **Tag summary.** Add `tag_counts()` / `tagCounts()` returning a map of tag → number of
  tasks with that tag.
- **Priority bump.** Add `bump_priority(id)` / `bumpPriority(id)` that raises a task's
  priority by one level (capped at high), and decide what should happen at the cap.

Walk us through your design choices, edge cases, and how you'd extend it further with more
time.

---

## What we're evaluating

- **Debugging method** — reading code, isolating faults, verifying fixes.
- **Communication** — narrating your reasoning and trade-offs.
- **Code quality** — clean, readable changes that match the surrounding style.
- **Feature judgment** — sensible design, edge-case awareness, and a test that proves it.

Good luck — and remember to think out loud!
