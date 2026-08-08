CREATE INDEX IF NOT EXISTS idx_tasks_user_priority
    ON task.tasks (user_id, priority);

CREATE INDEX IF NOT EXISTS idx_tasks_user_category
    ON task.tasks (user_id, category_id);

DROP INDEX IF EXISTS task.idx_task_tags_tag_id;

CREATE INDEX IF NOT EXISTS idx_task_tags_tag_task
    ON task.task_tags (tag_id, task_id);
