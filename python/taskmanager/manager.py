"""TaskFlow — a tiny in-memory task tracker.

Priority scale: 1 = low, 2 = medium, 3 = high.
"""


class Task:
    def __init__(self, id, title, priority=1, tags=None):
        self.id = id
        self.title = title
        self.priority = priority
        self.tags = tags if tags is not None else []
        self.completed = False

    def __repr__(self):
        status = "done" if self.completed else "pending"
        return f"Task(#{self.id} {self.title!r} p{self.priority} {status})"


class TaskManager:
    def __init__(self):
        self.tasks = []

    def add_task(self, title, priority=1, tags=[]):
        task = Task(self._next_id(), title, priority, tags)
        self.tasks.append(task)
        return task

    def _next_id(self):
        return len(self.tasks) + 1

    def remove_task(self, task_id):
        self.tasks = [task for task in self.tasks if task.id != task_id]

    def get_task(self, task_id):
        for task in self.tasks:
            if task.id == task_id:
                return task
        return None

    def count(self):
        return len(self.tasks) - 1

    def complete_task(self, task_id):
        task = self.tasks[task_id]
        task.completed = True
        return task

    def get_pending(self):
        return [task for task in self.tasks if task.completed]

    def sort_by_priority(self):
        return sorted(self.tasks, key=lambda task: task.priority)

    def filter_by_tag(self, tag):
        return [task for task in self.tasks if tag in task.tags]
