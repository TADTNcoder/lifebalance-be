ALTER TABLE task.categories
    ADD COLUMN IF NOT EXISTS owner_id UUID;

-- V2 created a global unique constraint on category names. Categories are now
-- private to their creator, while NULL-owned legacy/system rows stay shared and
-- read-only.
ALTER TABLE task.categories
    DROP CONSTRAINT IF EXISTS categories_name_key;

DROP INDEX IF EXISTS task.uq_categories_slug_active;

CREATE UNIQUE INDEX IF NOT EXISTS uq_categories_owner_name_active
    ON task.categories (owner_id, lower(trim(name)))
    WHERE owner_id IS NOT NULL
      AND deleted_at IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_categories_owner_slug_active
    ON task.categories (owner_id, lower(slug))
    WHERE owner_id IS NOT NULL
      AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_categories_visible_owner_name
    ON task.categories (owner_id, name)
    WHERE deleted_at IS NULL;
