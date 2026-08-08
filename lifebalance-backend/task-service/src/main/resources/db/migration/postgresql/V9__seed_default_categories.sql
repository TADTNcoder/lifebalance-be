-- Seed application-level default categories for the task module.
-- This migration is idempotent and restores system categories if they were soft-deleted.

WITH default_categories(name, slug, color, icon) AS (
    VALUES
        ('Work', 'work', '#1E88E5', 'briefcase'),
        ('Personal Development', 'personal-development', '#43A047', 'user'),
        ('Health & Fitness', 'health', '#E53935', 'heart'),
        ('Finance', 'finance', '#FB8C00', 'dollar-sign'),
        ('Learning', 'learning', '#8E24AA', 'book')
),
selected_existing_categories AS (
    SELECT DISTINCT ON (default_category.slug)
        category.id,
        default_category.slug AS seed_slug
    FROM default_categories default_category
    JOIN task.categories category
        ON lower(category.slug) = lower(default_category.slug)
        OR lower(category.name) = lower(default_category.name)
    ORDER BY
        default_category.slug,
        (category.deleted_at IS NULL) DESC,
        (lower(category.slug) = lower(default_category.slug)) DESC,
        category.created_at ASC
),
updated_categories AS (
    UPDATE task.categories category
    SET
        name = default_category.name,
        slug = default_category.slug,
        color = default_category.color,
        icon = default_category.icon,
        is_system = true,
        updated_at = now(),
        deleted_at = NULL
    FROM default_categories default_category
    JOIN selected_existing_categories existing_category
        ON existing_category.seed_slug = default_category.slug
    WHERE category.id = existing_category.id
    RETURNING category.id
)
INSERT INTO task.categories (
    id,
    name,
    slug,
    color,
    icon,
    is_system,
    created_at,
    updated_at,
    deleted_at
)
SELECT
    gen_random_uuid(),
    default_category.name,
    default_category.slug,
    default_category.color,
    default_category.icon,
    true,
    now(),
    now(),
    NULL
FROM default_categories default_category
WHERE NOT EXISTS (
    SELECT 1
    FROM task.categories category
    WHERE lower(category.slug) = lower(default_category.slug)
       OR lower(category.name) = lower(default_category.name)
);
