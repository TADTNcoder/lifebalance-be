CREATE TABLE IF NOT EXISTS task.task_tags (
    task_id UUID NOT NULL,
    tag_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_task_tags
        PRIMARY KEY (task_id, tag_id),

    CONSTRAINT fk_task_tags_task
        FOREIGN KEY (task_id)
        REFERENCES task.tasks (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_task_tags_tag
        FOREIGN KEY (tag_id)
        REFERENCES task.tags (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_task_tags_tag_id
    ON task.task_tags (tag_id);
