"""TaskFlow — a tiny in-memory task tracker.

Priority scale: 1 = low, 2 = medium, 3 = high.

The docstrings below describe what each method is *supposed* to do. The behavioral
tests in tests/test_manager.py check that intent. Some of the implementations don't
match their description — those are the bugs you're here to find.
"""


class Task:
    def __init__(self, id, title, priority=1, tags=None):
        self.id = id
        self.title = title
        self.priority = priority
        # A task with no tags should start with its own empty list.
        self.tags = tags if tags is not None else []
        self.completed = False

    def __repr__(self):
        status = "done" if self.completed else "pending"
        return f"Task(#{self.id} {self.title!r} p{self.priority} {status})"


class TaskManager:
    def __init__(self):
        self.tasks = []

    def add_task(self, title, priority=1, tags=[]):
        """Create a new task, store it, and return it.

        A task added without tags should get its own, independent empty tag list.
        """
        task = Task(self._next_id(), title, priority, tags)
        self.tasks.append(task)
        return task

    def _next_id(self):
        """Return a fresh id that is unique and never reused for the lifetime of this
        manager — even after tasks have been removed."""
        return len(self.tasks) + 1

    def remove_task(self, task_id):
        """Remove the task with the given id (a no-op if it isn't present)."""
        self.tasks = [task for task in self.tasks if task.id != task_id]

    def get_task(self, task_id):
        """Return the task with the given id, or None if there isn't one."""
        for task in self.tasks:
            if task.id == task_id:
                return task
        return None

    def count(self):
        """Return how many tasks are currently tracked — completed or not."""
        return sum(1 for task in self.tasks if not task.completed)

    def complete_task(self, task_id):
        """Find the task with the given id, mark it completed, and return it.

        Completing a task is idempotent: completing one that is already completed
        leaves it completed.
        """
        task = self.get_task(task_id)
        task.completed = not task.completed
        return task

    def get_pending(self):
        """Return every task that is NOT yet completed.

        This is a read-only query: it must not modify the manager's stored task list.
        """
        self.tasks = [task for task in self.tasks if not task.completed]
        return self.tasks

    def sort_by_priority(self):
        """Return the tasks ordered by priority, highest priority first.

        Tasks that share a priority keep their original insertion order (a stable sort).
        """
        return list(reversed(sorted(self.tasks, key=lambda task: task.priority)))

    def filter_by_tag(self, tag):
        """Return every task whose tags include the given tag."""
        return [task for task in self.tasks if tag in task.tags]

    def remove_completed(self):
        """Remove every completed task from the manager. Tasks still pending are kept."""
        for task in self.tasks:
            if task.completed:
                self.tasks.remove(task)

    def average_priority(self):
        """Return the mean priority across all tracked tasks as an exact value.

        The average keeps its fractional part: two tasks at priorities 3 and 2 average to
        exactly 2.5, not 2. (Assume there is at least one task.)
        """
        return sum(task.priority for task in self.tasks) // len(self.tasks)
