CREATE TABLE task.task_tags (
    task_id UUID NOT NULL,
    tag_id UUID NOT NULL,

    assigned_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (task_id, tag_id),

    CONSTRAINT fk_task_tags_task
        FOREIGN KEY (task_id)
        REFERENCES task.tasks(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_task_tags_tag
        FOREIGN KEY (tag_id)
        REFERENCES task.tags(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_task_tags_tag_id
ON task.task_tags(tag_id);