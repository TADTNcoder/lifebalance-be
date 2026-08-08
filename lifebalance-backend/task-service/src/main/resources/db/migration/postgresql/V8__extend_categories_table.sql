CREATE EXTENSION IF NOT EXISTS unaccent;

ALTER TABLE task.categories
    ADD COLUMN IF NOT EXISTS slug VARCHAR(100),
    ADD COLUMN IF NOT EXISTS color VARCHAR(20),
    ADD COLUMN IF NOT EXISTS icon VARCHAR(50),
    ADD COLUMN IF NOT EXISTS is_system BOOLEAN NOT NULL DEFAULT FALSE;

WITH normalized_categories AS (
    SELECT
        id,
        deleted_at,
        COALESCE(
            NULLIF(
                TRIM(BOTH '-' FROM REGEXP_REPLACE(
                    LOWER(UNACCENT(REPLACE(REPLACE(name, 'Đ', 'D'), 'đ', 'd'))),
                    '[^a-z0-9]+',
                    '-',
                    'g'
                )),
                ''
            ),
            'tag'
        ) AS slug_base
    FROM task.categories
    WHERE slug IS NULL
       OR TRIM(slug) = ''
),
ranked_categories AS (
    SELECT
        id,
        LEFT(slug_base, 100) AS slug_key,
        ROW_NUMBER() OVER (
            PARTITION BY LEFT(slug_base, 100), deleted_at IS NULL
            ORDER BY id
        ) AS duplicate_number
    FROM normalized_categories
),
resolved_categories AS (
    SELECT
        id,
        CASE
            WHEN duplicate_number = 1 THEN slug_key
            ELSE LEFT(slug_key, 67) || '-' || REPLACE(id::TEXT, '-', '')
        END AS resolved_slug
    FROM ranked_categories
)
UPDATE task.categories categories
SET
    slug = resolved_categories.resolved_slug,
    updated_at = now()
FROM resolved_categories
WHERE categories.id = resolved_categories.id;

ALTER TABLE task.categories
    ALTER COLUMN slug SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_categories_slug_active
    ON task.categories (slug)
    WHERE deleted_at IS NULL;
