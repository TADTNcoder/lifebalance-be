CREATE EXTENSION IF NOT EXISTS unaccent;

ALTER TABLE task.tags
    ADD COLUMN IF NOT EXISTS slug VARCHAR(100);

ALTER TABLE task.tags
    ADD COLUMN IF NOT EXISTS color VARCHAR(20);

ALTER TABLE task.tags
    ADD COLUMN IF NOT EXISTS is_system BOOLEAN;

UPDATE task.tags
SET is_system = FALSE
WHERE is_system IS NULL;

WITH normalized_tags AS (
    SELECT
        id,
        user_id,
        deleted_at,
        COALESCE(
            NULLIF(
                TRIM(BOTH '-' FROM REGEXP_REPLACE(LOWER(UNACCENT(name)), '[^a-z0-9]+', '-', 'g')),
                ''
            ),
            'tag'
        ) AS slug_base
    FROM task.tags
    WHERE slug IS NULL
),
ranked_tags AS (
    SELECT
        id,
        LEFT(slug_base, 100) AS slug_key,
        ROW_NUMBER() OVER (
            PARTITION BY user_id, LEFT(slug_base, 100), deleted_at IS NULL
            ORDER BY id
        ) AS duplicate_number
    FROM normalized_tags
),
resolved_tags AS (
    SELECT
        id,
        CASE
            WHEN duplicate_number = 1 THEN slug_key
            ELSE LEFT(slug_key, 67) || '-' || REPLACE(id::TEXT, '-', '')
        END AS resolved_slug
    FROM ranked_tags
)
UPDATE task.tags tags
SET slug = resolved_tags.resolved_slug
FROM resolved_tags
WHERE tags.id = resolved_tags.id;

ALTER TABLE task.tags
    ALTER COLUMN slug SET NOT NULL;

ALTER TABLE task.tags
    ALTER COLUMN is_system SET DEFAULT FALSE;

ALTER TABLE task.tags
    ALTER COLUMN is_system SET NOT NULL;

DROP INDEX IF EXISTS task.uq_tags_user_name_active;

CREATE UNIQUE INDEX IF NOT EXISTS uq_tags_user_slug_active
    ON task.tags (user_id, slug)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_tags_user_name_active
    ON task.tags (user_id, name)
    WHERE deleted_at IS NULL;
