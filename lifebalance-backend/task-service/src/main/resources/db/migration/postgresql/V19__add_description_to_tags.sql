ALTER TABLE task.tags
    ADD COLUMN IF NOT EXISTS description TEXT;
